package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GroupMemberEntity;
import com.groupmatch.app.domain.group.GroupMemberRole;
import com.groupmatch.app.domain.group.GroupStatus;

import java.time.LocalDateTime;

public class GroupSummaryResponse {

    private Long id;
    private String name;
    private String description;
    private GroupStatus status;
    private GroupMemberRole role;
    private LocalDateTime joinedAt;

    public GroupSummaryResponse(GroupMemberEntity membership) {
        this.id = membership.getGroup().getId();
        this.name = membership.getGroup().getName();
        this.description = membership.getGroup().getDescription();
        this.status = membership.getGroup().getStatus();
        this.role = membership.getRole();
        this.joinedAt = membership.getJoinedAt();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public GroupStatus getStatus() { return status; }
    public GroupMemberRole getRole() { return role; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}
