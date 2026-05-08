package com.groupmatch.app.notification;

import com.groupmatch.app.domain.notification.NotificationEntity;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private String title;
    private String body;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationResponse(NotificationEntity notification) {
        this.id = notification.getId();
        this.title = notification.getTitle();
        this.body = notification.getBody();
        this.read = notification.isRead();
        this.createdAt = notification.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
