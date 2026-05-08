package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GroupMemberEntity;
import com.groupmatch.app.domain.group.GroupMemberRole;

import java.time.LocalDateTime;

public class MemberResponse {

    private Long id;
    private Long userId;
    private String name;
    private String email;
    private GroupMemberRole role;
    private boolean muted;
    private LocalDateTime joinedAt;

    public MemberResponse(GroupMemberEntity member) {
        this.id = member.getId();
        this.userId = member.getUser().getId();
        this.name = member.getUser().getName();
        this.email = member.getUser().getEmail();
        this.role = member.getRole();
        this.muted = member.isMuted();
        this.joinedAt = member.getJoinedAt();
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public GroupMemberRole getRole() { return role; }
    public boolean isMuted() { return muted; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}
