package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GroupMemberEntity;
import com.groupmatch.app.domain.group.GroupMemberRole;
import com.groupmatch.app.domain.group.GroupStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class GroupSummaryResponse {

    private UUID uuid;
    private Long id;
    private String name;
    private String description;
    private GroupStatus status;
    private GroupMemberRole role;
    private LocalDateTime joinedAt;
    private String coverImageBase64;

    public GroupSummaryResponse(GroupMemberEntity membership) {
        this.uuid = membership.getGroup().getUuid();
        this.id = membership.getGroup().getId();
        this.name = membership.getGroup().getName();
        this.description = membership.getGroup().getDescription();
        this.status = membership.getGroup().getStatus();
        this.role = membership.getRole();
        this.joinedAt = membership.getJoinedAt();
        this.coverImageBase64 = membership.getGroup().getCoverImageBase64();
    }

    public UUID getUuid() { return uuid; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public GroupStatus getStatus() { return status; }
    public GroupMemberRole getRole() { return role; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public String getCoverImageBase64() { return coverImageBase64; }
}
