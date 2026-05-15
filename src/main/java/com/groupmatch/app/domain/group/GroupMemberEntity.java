package com.groupmatch.app.domain.group;

import com.groupmatch.app.domain.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "group_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_id"})
)
public class GroupMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @Column(nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupMemberRole role = GroupMemberRole.MEMBER;

    @Column(nullable = false)
    private boolean muted = false;

    protected GroupMemberEntity() {}

    public GroupMemberEntity(UserEntity user, GroupEntity group) {
        this.user = user;
        this.group = group;
        this.joinedAt = LocalDateTime.now();
    }

    public void promote()   { this.role = GroupMemberRole.ADMIN;  }
    public void setOwner()  { this.role = GroupMemberRole.OWNER;  }
    public void demote()    { this.role = GroupMemberRole.MEMBER; }
    public void toggleMute() { this.muted = !this.muted; }

    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public GroupEntity getGroup() { return group; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public GroupMemberRole getRole() { return role; }
    public boolean isMuted() { return muted; }
}
