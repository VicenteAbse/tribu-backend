package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GenderPreference;
import com.groupmatch.app.domain.group.GroupEntity;
import com.groupmatch.app.domain.group.GroupStatus;

import java.time.LocalDateTime;

public class GroupDiscoveryResponse {

    private Long id;
    private String name;
    private String description;
    private String creatorName;
    private GenderPreference genderPreference;
    private Integer minMembers;
    private Integer maxMembers;
    private Integer likesCount;
    private GroupStatus status;
    private LocalDateTime createdAt;

    public GroupDiscoveryResponse(GroupEntity group) {
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.creatorName = group.getCreator().getName() != null
            ? group.getCreator().getName()
            : group.getCreator().getEmail();
        this.genderPreference = group.getGenderPreference();
        this.minMembers = group.getMinMembers();
        this.maxMembers = group.getMaxMembers();
        this.likesCount = group.getLikesCount();
        this.status = group.getStatus();
        this.createdAt = group.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCreatorName() { return creatorName; }
    public GenderPreference getGenderPreference() { return genderPreference; }
    public Integer getMinMembers() { return minMembers; }
    public Integer getMaxMembers() { return maxMembers; }
    public Integer getLikesCount() { return likesCount; }
    public GroupStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
