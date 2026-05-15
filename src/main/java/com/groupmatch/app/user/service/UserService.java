package com.groupmatch.app.user.service;

import com.groupmatch.app.auth.UserRepository;
import com.groupmatch.app.domain.user.UserEntity;
import com.groupmatch.app.group.GroupMemberRepository;
import com.groupmatch.app.group.GroupSummaryResponse;
import com.groupmatch.app.user.UserProfileRequest;
import com.groupmatch.app.user.UserProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    public UserService(UserRepository userRepository, GroupMemberRepository groupMemberRepository) {
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        return new UserProfileResponse(findByEmail(email));
    }

    @Transactional
    public UserProfileResponse updateProfile(UserProfileRequest request, String email) {
        UserEntity user = findByEmail(email);
        user.updateProfile(request.getName(), request.getDescription(), request.getSearchRadiusKm());
        userRepository.save(user);
        return new UserProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateAvatar(String imageBase64, String email) {
        UserEntity user = findByEmail(email);
        user.setAvatarBase64(imageBase64);
        userRepository.save(user);
        return new UserProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public List<GroupSummaryResponse> getMyGroups(String email) {
        UserEntity user = findByEmail(email);
        return groupMemberRepository.findByUserId(user.getId())
            .stream()
            .map(GroupSummaryResponse::new)
            .toList();
    }

    private UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }
}
