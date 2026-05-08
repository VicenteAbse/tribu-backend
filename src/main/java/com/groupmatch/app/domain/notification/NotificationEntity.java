package com.groupmatch.app.domain.notification;

import com.groupmatch.app.domain.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String body;

    @Column(nullable = false)
    private boolean read = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected NotificationEntity() {}

    public NotificationEntity(UserEntity user, String title, String body) {
        this.user = user;
        this.title = title;
        this.body = body;
        this.createdAt = LocalDateTime.now();
    }

    public void markAsRead() { this.read = true; }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
