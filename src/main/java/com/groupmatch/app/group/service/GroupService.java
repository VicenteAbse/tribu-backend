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
import java.util.UUID;

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
        creatorMembership.setOwner();
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

        List<GroupEntity> all = groupRepository.findDiscoverableGroups(
            user.getId(), List.of(GroupStatus.OPEN, GroupStatus.ACTIVE), gender);

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
    public GroupDetailResponse getDetail(UUID groupUuid, String userEmail) {
        GroupEntity group = findGroupByUuid(groupUuid);
        List<GroupMemberEntity> members = groupMemberRepository.findByGroupId(group.getId());
        return new GroupDetailResponse(group, members);
    }

    @Transactional
    public GroupDetailResponse updateGroup(UUID groupUuid, UpdateGroupRequest request, String userEmail) {
        UserEntity user = findUserByEmail(userEmail);
        GroupEntity group = findGroupByUuid(groupUuid);
        requireAdmin(user.getId(), group.getId());
        group.update(request.getName(), request.getDescription(), request.getJoinPolicy());
        groupRepository.save(group);
        List<GroupMemberEntity> members = groupMemberRepository.findByGroupId(group.getId());
        return new GroupDetailResponse(group, members);
    }

    @Transactional
    public GroupDetailResponse updateCoverImage(UUID groupUuid, String imageBase64, String userEmail) {
        UserEntity user = findUserByEmail(userEmail);
        GroupEntity group = findGroupByUuid(groupUuid);
        requireAdmin(user.getId(), group.getId());
        group.setCoverImageBase64(imageBase64);
        groupRepository.save(group);
        List<GroupMemberEntity> members = groupMemberRepository.findByGroupId(group.getId());
        return new GroupDetailResponse(group, members);
    }

    @Transactional
    public SwipeResponse swipe(UUID groupUuid, SwipeRequest request, String userEmail) {
        UserEntity user = findUserByEmail(userEmail);
        GroupEntity group = findGroupByUuid(groupUuid);
        Long groupId = group.getId();

        if (group.getStatus() == GroupStatus.CLOSED) {
            throw new IllegalStateException("Este grupo ya está completo");
        }
        if (group.getCreator().getId().equals(user.getId())) {
            throw new IllegalArgumentException("No puedes dar swipe a tu propio grupo");
        }
        if (groupSwipeRepository.existsByUserIdAndGroupId(user.getId(), groupId)) {
            throw new IllegalArgumentException("Ya hiciste swipe en este grupo");
        }
        if (groupMemberRepository.existsByUserIdAndGroupId(user.getId(), groupId)) {
            throw new IllegalArgumentException("Ya eres miembro de este grupo");
        }

        boolean liked = request.getLiked();

        if (group.getStatus() == GroupStatus.ACTIVE) {
            // Grupo ya activo: unirse o solicitar ingreso
            groupSwipeRepository.save(new GroupSwipeEntity(user, group, liked));
            if (liked) {
                if (group.getJoinPolicy() == GroupJoinPolicy.OPEN) {
                    groupMemberRepository.save(new GroupMemberEntity(user, group));
                    notificationService.create(
                        user,
                        "¡Te uniste al grupo!",
                        "Ahora eres miembro de \"" + group.getName() + "\". ¡El chat está disponible!"
                    );
                } else {
                    boolean alreadyRequested = joinRequestRepository
                        .existsByUserIdAndGroupId(user.getId(), groupId);
                    if (!alreadyRequested) {
                        joinRequestRepository.save(new JoinRequestEntity(user, group));
                        notificationService.create(
                            group.getCreator(),
                            "Nueva solicitud de ingreso",
                            user.getName() + " quiere unirse a \"" + group.getName() + "\""
                        );
                    }
                }
            }
            return new SwipeResponse(liked, group.getStatus(), false, user.getDailyLikesLeft());
        }

        // Grupo OPEN: lógica original de activación por likes
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
    public List<MemberResponse> getMembers(UUID groupUuid, String userEmail) {
        GroupEntity group = findGroupByUuid(groupUuid);
        return groupMemberRepository.findByGroupId(group.getId())
            .stream()
            .map(MemberResponse::new)
            .toList();
    }

    @Transactional
    public void removeMember(UUID groupUuid, Long memberId, String userEmail) {
        UserEntity requester = findUserByEmail(userEmail);
        GroupEntity group = findGroupByUuid(groupUuid);
        Long groupId = group.getId();

        GroupMemberEntity requesterMembership = groupMemberRepository.findByGroupIdAndUserId(groupId, requester.getId())
            .orElseThrow(() -> new IllegalStateException("No tienes permisos para esta acción"));

        GroupMemberEntity target = groupMemberRepository.findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("Miembro no encontrado"));
        if (!target.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("El miembro no pertenece a este grupo");
        }

        boolean requesterIsOwner = requesterMembership.getRole() == GroupMemberRole.OWNER;
        boolean requesterIsAdmin = requesterMembership.getRole() == GroupMemberRole.ADMIN;
        boolean targetIsOwner    = target.getRole() == GroupMemberRole.OWNER;
        boolean targetIsAdmin    = target.getRole() == GroupMemberRole.ADMIN;

        if (targetIsOwner) throw new IllegalStateException("No se puede expulsar al dueño del grupo");
        if (!requesterIsOwner && !requesterIsAdmin) throw new IllegalStateException("No tienes permisos para esta acción");
        if (requesterIsAdmin && targetIsAdmin) throw new IllegalStateException("Los admin no pueden expulsar a otros admin");

        groupMemberRepository.delete(target);
    }

    @Transactional
    public MemberResponse promoteMember(UUID groupUuid, Long memberId, String userEmail) {
        UserEntity owner = findUserByEmail(userEmail);
        GroupEntity group = findGroupByUuid(groupUuid);
        requireOwner(owner.getId(), group.getId());

        GroupMemberEntity target = groupMemberRepository.findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("Miembro no encontrado"));
        if (!target.getGroup().getId().equals(group.getId())) {
            throw new IllegalArgumentException("El miembro no pertenece a este grupo");
        }
        if (target.getRole() != GroupMemberRole.MEMBER) {
            throw new IllegalStateException("Solo se puede promover a miembros");
        }
        target.promote();
        groupMemberRepository.save(target);
        return new MemberResponse(target);
    }

    @Transactional
    public MemberResponse demoteMember(UUID groupUuid, Long memberId, String userEmail) {
        UserEntity owner = findUserByEmail(userEmail);
        GroupEntity group = findGroupByUuid(groupUuid);
        requireOwner(owner.getId(), group.getId());

        GroupMemberEntity target = groupMemberRepository.findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("Miembro no encontrado"));
        if (!target.getGroup().getId().equals(group.getId())) {
            throw new IllegalArgumentException("El miembro no pertenece a este grupo");
        }
        if (target.getRole() != GroupMemberRole.ADMIN) {
            throw new IllegalStateException("Solo se puede quitar el rol a admins");
        }
        target.demote();
        groupMemberRepository.save(target);
        return new MemberResponse(target);
    }

    @Transactional
    public MemberResponse muteMember(UUID groupUuid, Long memberId, String userEmail) {
        UserEntity requester = findUserByEmail(userEmail);
        GroupEntity group = findGroupByUuid(groupUuid);
        Long groupId = group.getId();

        GroupMemberEntity requesterMembership = groupMemberRepository.findByGroupIdAndUserId(groupId, requester.getId())
            .orElseThrow(() -> new IllegalStateException("No tienes permisos para esta acción"));

        GroupMemberEntity target = groupMemberRepository.findById(memberId)
            .orElseThrow(() -> new NoSuchElementException("Miembro no encontrado"));
        if (!target.getGroup().getId().equals(groupId)) {
            throw new IllegalArgumentException("El miembro no pertenece a este grupo");
        }

        boolean requesterIsOwner = requesterMembership.getRole() == GroupMemberRole.OWNER;
        boolean requesterIsAdmin = requesterMembership.getRole() == GroupMemberRole.ADMIN;
        boolean targetIsOwner    = target.getRole() == GroupMemberRole.OWNER;
        boolean targetIsAdmin    = target.getRole() == GroupMemberRole.ADMIN;

        if (targetIsOwner) throw new IllegalStateException("No se puede silenciar al dueño del grupo");
        if (!requesterIsOwner && !requesterIsAdmin) throw new IllegalStateException("No tienes permisos para esta acción");
        if (requesterIsAdmin && targetIsAdmin) throw new IllegalStateException("Los admin no pueden silenciar a otros admin");

        target.toggleMute();
        groupMemberRepository.save(target);
        return new MemberResponse(target);
    }

    @Transactional(readOnly = true)
    public List<JoinRequestResponse> getJoinRequests(UUID groupUuid, String userEmail) {
        UserEntity user = findUserByEmail(userEmail);
        GroupEntity group = findGroupByUuid(groupUuid);
        requireAdmin(user.getId(), group.getId());
        return joinRequestRepository.findByGroupIdAndStatus(group.getId(), JoinRequestStatus.PENDING)
            .stream()
            .map(JoinRequestResponse::new)
            .toList();
    }

    @Transactional
    public JoinRequestResponse approveJoinRequest(UUID groupUuid, Long requestId, String userEmail) {
        UserEntity admin = findUserByEmail(userEmail);
        GroupEntity group = findGroupByUuid(groupUuid);
        requireAdmin(admin.getId(), group.getId());

        JoinRequestEntity joinRequest = joinRequestRepository.findByGroupIdAndId(group.getId(), requestId)
            .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada"));
        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new IllegalStateException("La solicitud ya fue procesada");
        }
        joinRequest.approve();
        joinRequestRepository.save(joinRequest);

        if (!groupMemberRepository.existsByUserIdAndGroupId(joinRequest.getUser().getId(), group.getId())) {
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
    public JoinRequestResponse rejectJoinRequest(UUID groupUuid, Long requestId, String userEmail) {
        UserEntity admin = findUserByEmail(userEmail);
        GroupEntity group = findGroupByUuid(groupUuid);
        requireAdmin(admin.getId(), group.getId());

        JoinRequestEntity joinRequest = joinRequestRepository.findByGroupIdAndId(group.getId(), requestId)
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
    public GroupEventResponse createEvent(UUID groupUuid, GroupEventRequest request, String userEmail) {
        UserEntity user = findUserByEmail(userEmail);
        GroupEntity group = findGroupByUuid(groupUuid);
        requireAdmin(user.getId(), group.getId());

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
    public List<GroupEventResponse> getEvents(UUID groupUuid, String userEmail) {
        GroupEntity group = findGroupByUuid(groupUuid);
        return groupEventRepository.findByGroupIdOrderByEventDateAsc(group.getId())
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

    private void requireAdmin(Long userId, Long groupId) {
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            .filter(m -> m.getRole() == GroupMemberRole.ADMIN || m.getRole() == GroupMemberRole.OWNER)
            .orElseThrow(() -> new IllegalStateException("No tienes permisos para esta acción"));
    }

    private void requireOwner(Long userId, Long groupId) {
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
            .filter(m -> m.getRole() == GroupMemberRole.OWNER)
            .orElseThrow(() -> new IllegalStateException("Solo el dueño del grupo puede realizar esta acción"));
    }

    private UserEntity findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }

    private GroupEntity findGroupByUuid(UUID uuid) {
        return groupRepository.findByUuid(uuid)
            .orElseThrow(() -> new NoSuchElementException("Grupo no encontrado"));
    }
}
