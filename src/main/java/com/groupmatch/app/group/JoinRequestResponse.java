package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.JoinRequestEntity;
import com.groupmatch.app.domain.group.JoinRequestStatus;

import java.time.LocalDateTime;

public class JoinRequestResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private JoinRequestStatus status;
    private LocalDateTime createdAt;

    public JoinRequestResponse(JoinRequestEntity request) {
        this.id = request.getId();
        this.userId = request.getUser().getId();
        this.name = request.getUser().getName();
        this.email = request.getUser().getEmail();
        this.status = request.getStatus();
        this.createdAt = request.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public JoinRequestStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
