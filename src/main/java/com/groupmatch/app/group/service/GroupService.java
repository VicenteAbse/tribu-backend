package com.groupmatch.app.group.service;

import com.groupmatch.app.auth.UserRepository;
import com.groupmatch.app.domain.group.*;
import com.groupmatch.app.domain.user.Gender;
import com.groupmatch.app.domain.user.UserEntity;
import com.groupmatch.app.group.*;
import org.springframework.data.domain.Page;
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
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository,
                        GroupSwipeRepository groupSwipeRepository,
                        GroupMemberRepository groupMemberRepository,
                        UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.groupSwipeRepository = groupSwipeRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.userRepository = userRepository;
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
        groupRepository.save(group);

        return new GroupDetailResponse(group, List.of());
    }

    @Transactional(readOnly = true)
    public Page<GroupDiscoveryResponse> discover(String userEmail, Pageable pageable) {
        UserEntity user = findUserByEmail(userEmail);
        String gender = user.getGender() != null ? user.getGender().name() : Gender.OTHER.name();
        return groupRepository
            .findDiscoverableGroups(user.getId(), GroupStatus.OPEN, gender, pageable)
            .map(GroupDiscoveryResponse::new);
    }

    @Transactional(readOnly = true)
    public GroupDetailResponse getDetail(Long groupId, String userEmail) {
        GroupEntity group = findGroupById(groupId);
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

            if (group.getStatus() == GroupStatus.ACTIVE) {
                activateGroupMembers(group);
            }
        }

        groupSwipeRepository.save(new GroupSwipeEntity(user, group, liked));

        boolean justActivated = statusBefore == GroupStatus.OPEN && group.getStatus() == GroupStatus.ACTIVE;
        return new SwipeResponse(liked, group.getStatus(), justActivated, user.getDailyLikesLeft());
    }

    private void activateGroupMembers(GroupEntity group) {
        List<GroupSwipeEntity> likers = groupSwipeRepository.findLikersByGroupId(group.getId());
        for (GroupSwipeEntity swipe : likers) {
            if (!groupMemberRepository.existsByUserIdAndGroupId(swipe.getUser().getId(), group.getId())) {
                groupMemberRepository.save(new GroupMemberEntity(swipe.getUser(), group));
            }
        }
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
