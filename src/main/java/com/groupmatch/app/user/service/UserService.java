package com.groupmatch.app.user.service;

import com.groupmatch.app.auth.UserRepository;
import com.groupmatch.app.domain.user.UserEntity;
import com.groupmatch.app.user.UserProfileRequest;
import com.groupmatch.app.user.UserProfileResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String email) {
        return new UserProfileResponse(findByEmail(email));
    }

    @Transactional
    public UserProfileResponse updateProfile(UserProfileRequest request, String email) {
        UserEntity user = findByEmail(email);
        user.updateProfile(request.getName(), request.getGender(), request.getBirthDate(), request.getSearchRadiusKm());
        userRepository.save(user);
        return new UserProfileResponse(user);
    }

    private UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }
}
