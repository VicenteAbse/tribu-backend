package com.groupmatch.app.domain.group;

import com.groupmatch.app.domain.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "join_requests",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id"})
)
public class JoinRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JoinRequestStatus status = JoinRequestStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected JoinRequestEntity() {}

    public JoinRequestEntity(UserEntity user, GroupEntity group) {
        this.user = user;
        this.group = group;
        this.createdAt = LocalDateTime.now();
    }

    public void approve() { this.status = JoinRequestStatus.APPROVED; }
    public void reject()  { this.status = JoinRequestStatus.REJECTED; }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public GroupEntity getGroup() { return group; }
    public JoinRequestStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
