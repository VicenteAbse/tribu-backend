package com.groupmatch.app.group.service;

import com.groupmatch.app.auth.UserRepository;
import com.groupmatch.app.domain.group.*;
import com.groupmatch.app.domain.user.Gender;
import com.groupmatch.app.domain.user.UserEntity;
import com.groupmatch.app.group.*;
import com.groupmatch.app.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupSwipeRepository groupSwipeRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final JoinRequestRepository joinRequestRepository;
    private final GroupEventRepository groupEventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public GroupService(GroupRepository groupRepository,
                        GroupSwipeRepository groupSwipeRepository,
                        GroupMemberRepository groupMemberRepository,
                        JoinRequestRepository joinRequestRepository,
                        GroupEventRepository groupEventRepository,
                        UserRepository userRepository,
                        NotificationService notificationService) {
        this.groupRepository = groupRepository;
        this.groupSwipeRepository = groupSwipeRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.joinRequestRepository = joinRequestRepository;
        this.groupEventRepository = groupEventRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public GroupDetailResponse create(CreateGroupRequest request, String creatorEmail) {
        UserEntity creator = findUserByEmail(creatorEmail);

        if (request.getMinMembers() > request.getMaxMembers()) {
            throw new IllegalArgumentException("El mínimo de miembros no puede ser mayor que el máximo");
        }

        GroupEntity group = new GroupEntity(
            request.getName(),
            request.getDescription(),
            creator,
            request.getGenderPreference(),
            request.getMinMembers(),
            request.getMaxMembers()
        );
        group.setCategory(request.getCategory());
        group.setLatitude(request.getLatitude());
        group.setLongitude(request.getLongitude());
        if (request.getJoinPolicy() != null) group.setJoinPolicy(request.getJoinPolicy());
        groupRepository.save(group);

        GroupMemberEntity creatorMembership = new GroupMemberEntity(creator, group);
        creatorMembership.promote();
        groupMemberRepository.save(creatorMembership);

        return new GroupDetailResponse(group, List.of(creatorMembership));
    }

    @Transactional(readOnly = true)
    public Page<GroupDiscoveryResponse> discover(
            String userEmail,
            Double latitude,
            Double longitude,
            Double radiusKm,
            GroupCategory category,
            Pageable pageable) {

        UserEntity user = findUserByEmail(userEmail);
        String gender = user.getGender() != null ? user.getGender().name() : Gender.OTHER.name();

        List<GroupEntity> all = groupRepository.findDiscoverableGroups(user.getId(), GroupStatus.OPEN, gender);

        boolean filterByDistance = latitude != null && longitude != null && radiusKm != null;

        List<GroupEntity> filtered = all.stream()
            .filter(g -> {
                if (!filterByDistance) return true;
                if (g.getLatitude() == null || g.getLongitude() == null) return true;
                return haversine(latitude, longitude, g.getLatitude(), g.getLongitude()) <= radiusKm;
            })
            .filter(g -> category == null || category.equals(g.getCategory()))
            .toList();

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<GroupEntity> pageContent = start >= filtered.size() ? List.of() : filtered.subList(start, end);

        List<GroupDiscoveryResponse> responses = pageContent.stream()
            .map(g -> {
                Double dist = (filterByDistance && g.getLatitude() != null && g.getLongitude() != null)
                    ? Math.round(haversine(latitude, longitude, g.getLatitude(), g.getLongitude()) * 10.0) / 10.0
                    : null;
                return new GroupDiscoveryResponse(g, dist);
            })
            .toList();

        return new PageImpl<>(responses, pageable, filtered.size());
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Transactional(readOnly = true)
    public GroupDetailResponse getDetail(Long groupId, String userEmail) {
        GroupEntity group = findGroupById(groupId);
        List<GroupMemberEntity> members = groupMemberRepository.findByGroupId(groupId);
        return new GroupDetailResponse(group, members);
    }

    @Transactional
    public GroupDetailResponse updateGroup(Long groupId, UpdateGroupRequest request, String userEmail) {
        UserEntity user = findUserByEmail(userEmail);
        GroupEntity group = findGroupById(groupId);
        requireAdmin(user.getId(), groupId, group);
        group.update(request.getName(), request.getDescription(), request.getJoinPolicy());
        groupRepository.save(group);
        List<GroupMemberEntity> members = groupMemberRepository.findByGroupId(groupId);
        return new GroupDetailResponse(group, members);
    }

    @Transactional
    public SwipeResponse swipe(Long groupId, SwipeRequest request, String userEmail) {
        UserEntity user = findUserByEmail(userEmail);
        GroupEntity group = findGroupById(groupId);

        if (group.getStatus() != GroupStatus.OPEN) {
            throw new IllegalStateException("Este grupo ya no está disponible para swipe");
        }
        if (group.getCreator().getId().equals(user.getId())) {
            throw new IllegalArgumentException("No puedes dar swipe a tu propio grupo");
        }
        if (groupSwipeRepository.existsByUserIdAndGroupId(user.getId(), groupId)) {
            throw new IllegalArgumentException("Ya hiciste swipe en este grupo");
        }

        boolean liked = request.getLiked();
        GroupStatus statusBefore = group.getStatus();

        if (liked) {
            if (!user.hasLikesLeft(LocalDate.now())) {
                throw new IllegalStateException("No te quedan likes por hoy. Vuelve mañana");
            }
            user.consumeLike(LocalDate.now());
            userRepository.save(user);
            group.incrementLikes();
            groupRepository.save(group);
            groupSwipeRepository.save(new GroupSwipeEntity(user, group, liked));

            if (group.getStatus() == GroupStatus.ACTIVE) {
                activateGroupMembers(group);
                notificationService.create(
                    group.getCreator(),
                    "¡Tu grupo está activo!",
                    "El grupo \"" + group.getName() + "\" alcanzó el mínimo de miembros. ¡El chat ya está disponible!"
                );
            }
        } else {
            groupSwipeRepository.save(new GroupSwipeEntity(user, group, liked));
        }

        boolean justActivated = statusBefore == GroupStatus.OPEN && group.getStatus() == GroupStatus.ACTIVE;
        return new SwipeResponse(liked, group.getStatus(), justActivated, user.getDailyLikesLeft());
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> getMembers(Long groupId, String userEmail) {
        findGroupById(groupId);
        return groupMemberRepository.findByGroupId(groupId)
            .stream()
            .map(MemberResponse::new)
            .toList();
    }

    @Transactional
    public void removeMember(Long groupId, Long memberId, String userEmail) {
        UserEntity admin = findUserByEmail(userEmail);
        GroupEntity group = findGroupById(groupId);
        requireAdmin(admin.getId(), groupId, group);

        GroupMemberEntity target = groupMemberRepository.findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("Miembro no encontrado"));
        if (!target.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("El miembro no pertenece a este grupo");
        }
        groupMemberRepository.delete(target);
    }

    @Transactional
    public MemberResponse promoteMember(Long groupId, Long memberId, String userEmail) {
        UserEntity admin = findUserByEmail(userEmail);
        GroupEntity group = findGroupById(groupId);
        requireAdmin(admin.getId(), groupId, group);

        GroupMemberEntity target = groupMemberRepository.findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("Miembro no encontrado"));
        if (!target.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("El miembro no pertenece a este grupo");
        }
        target.promote();
        groupMemberRepository.save(target);
        return new MemberResponse(target);
    }

    @Transactional
    public MemberResponse muteMember(Long groupId, Long memberId, String userEmail) {
        UserEntity admin = findUserByEmail(userEmail);
        GroupEntity group = findGroupById(groupId);
        requireAdmin(admin.getId(), groupId, group);

        GroupMemberEntity target = groupMemberRepository.findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("Miembro no encontrado"));
        if (!target.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("El miembro no pertenece a este grupo");
        }
        target.toggleMute();
        groupMemberRepository.save(target);
        return new MemberResponse(target);
    }

    @Transactional(readOnly = true)
    public List<JoinRequestResponse> getJoinRequests(Long groupId, String userEmail) {
        UserEntity user = findUserByEmail(userEmail);
        GroupEntity group = findGroupById(groupId);
        requireAdmin(user.getId(), groupId, group);
        return joinRequestRepository.findByGroupIdAndStatus(groupId, JoinRequestStatus.PENDING)
            .stream()
            .map(JoinRequestResponse::new)
            .toList();
    }

    @Transactional
    public JoinRequestResponse approveJoinRequest(Long groupId, Long requestId, String userEmail) {
        UserEntity admin = findUserByEmail(userEmail);
        GroupEntity group = findGroupById(groupId);
        requireAdmin(admin.getId(), groupId, group);

        JoinRequestEntity joinRequest = joinRequestRepository.findByGroupIdAndId(groupId, requestId)
            .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new IllegalStateException("La solicitud ya fue procesada");
        }
        joinRequest.approve();
        joinRequestRepository.save(joinRequest);

        if (!groupMemberRepository.existsByUserIdAndGroupId(joinRequest.getUser().getId(), groupId)) {
            groupMemberRepository.save(new GroupMemberEntity(joinRequest.getUser(), group));
        }

        notificationService.create(
            joinRequest.getUser(),
            "Solicitud aprobada",
            "Tu solicitud para unirte al grupo \"" + group.getName() + "\" fue aprobada."
        );

        return new JoinRequestResponse(joinRequest);
    }

    @Transactional
    public JoinRequestResponse rejectJoinRequest(Long groupId, Long requestId, String userEmail) {
        UserEntity admin = findUserByEmail(userEmail);
        GroupEntity group = findGroupById(groupId);
        requireAdmin(admin.getId(), groupId, group);

        JoinRequestEntity joinRequest = joinRequestRepository.findByGroupIdAndId(groupId, requestId)
            .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new IllegalStateException("La solicitud ya fue procesada");
        }
        joinRequest.reject();
        joinRequestRepository.save(joinRequest);

        notificationService.create(
            joinRequest.getUser(),
            "Solicitud rechazada",
            "Tu solicitud para unirte al grupo \"" + group.getName() + "\" fue rechazada."
        );

        return new JoinRequestResponse(joinRequest);
    }

    @Transactional
    public GroupEventResponse createEvent(Long groupId, GroupEventRequest request, String userEmail) {
        UserEntity user = findUserByEmail(userEmail);
        GroupEntity group = findGroupById(groupId);
        requireAdmin(user.getId(), groupId, group);

        GroupEventEntity event = new GroupEventEntity(
            group, user,
            request.getTitle(),
            request.getDescription(),
            request.getEventDate(),
            request.getLocation()
        );
        groupEventRepository.save(event);
        return new GroupEventResponse(event);
    }

    @Transactional(readOnly = true)
    public List<GroupEventResponse> getEvents(Long groupId, String userEmail) {
        findGroupById(groupId);
        return groupEventRepository.findByGroupIdOrderByEventDateAsc(groupId)
            .stream()
            .map(GroupEventResponse::new)
            .toList();
    }

    private void activateGroupMembers(GroupEntity group) {
        List<GroupSwipeEntity> likers = groupSwipeRepository.findLikersByGroupId(group.getId());
        for (GroupSwipeEntity swipe : likers) {
            if (!groupMemberRepository.existsByUserIdAndGroupId(swipe.getUser().getId(), group.getId())) {
                groupMemberRepository.save(new GroupMemberEntity(swipe.getUser(), group));
            }
            notificationService.create(
                swipe.getUser(),
                "¡El chat está abierto!",
                "El grupo \"" + group.getName() + "\" ya tiene suficientes miembros. ¡Empieza a chatear!"
            );
        }
    }

    private void requireAdmin(Long userId, Long groupId, GroupEntity group) {
        if (group.getCreator().getId().equals(userId)) return;
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            .filter(m -> m.getRole() == GroupMemberRole.ADMIN)
            .orElseThrow(() -> new IllegalStateException("No tienes permisos para esta acción"));
    }

    private UserEntity findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    private GroupEntity findGroupById(Long groupId) {
        return groupRepository.findById(groupId)
            .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));
    }
}
