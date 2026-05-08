package com.groupmatch.app.notification.service;

import com.groupmatch.app.auth.UserRepository;
import com.groupmatch.app.domain.notification.NotificationEntity;
import com.groupmatch.app.domain.user.UserEntity;
import com.groupmatch.app.notification.NotificationRepository;
import com.groupmatch.app.notification.NotificationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository,
                                UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void create(UserEntity user, String title, String body) {
        notificationRepository.save(new NotificationEntity(user, title, body));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String userEmail) {
        UserEntity user = findUserByEmail(userEmail);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream()
            .map(NotificationResponse::new)
            .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String userEmail) {
        UserEntity user = findUserByEmail(userEmail);
        NotificationEntity notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NoSuchElementException("Notificación no encontrada"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("No tienes acceso a esta notificación");
        }
        notification.markAsRead();
        notificationRepository.save(notification);
        return new NotificationResponse(notification);
    }

    private UserEntity findUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }
}
