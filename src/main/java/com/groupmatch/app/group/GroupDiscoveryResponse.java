package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GenderPreference;
import com.groupmatch.app.domain.group.GroupCategory;
import com.groupmatch.app.domain.group.GroupEntity;
import com.groupmatch.app.domain.group.GroupStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class GroupDiscoveryResponse {

    private UUID uuid;
    private Long id;
    private String name;
    private String description;
    private String creatorName;
    private GenderPreference genderPreference;
    private GroupCategory category;
    private Integer minMembers;
    private Integer maxMembers;
    private Integer likesCount;
    private GroupStatus status;
    private LocalDateTime createdAt;
    private Double distanceKm;
    private String coverImageBase64;

    public GroupDiscoveryResponse(GroupEntity group, Double distanceKm) {
        this.uuid = group.getUuid();
        this.id = group.getId();
        this.name = group.getName();
        this.description = group.getDescription();
        this.creatorName = group.getCreator().getName() != null
            ? group.getCreator().getName()
            : group.getCreator().getEmail();
        this.genderPreference = group.getGenderPreference();
        this.category = group.getCategory();
        this.minMembers = group.getMinMembers();
        this.maxMembers = group.getMaxMembers();
        this.likesCount = group.getLikesCount();
        this.status = group.getStatus();
        this.createdAt = group.getCreatedAt();
        this.distanceKm = distanceKm;
        this.coverImageBase64 = group.getCoverImageBase64();
    }

    public UUID getUuid() { return uuid; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCreatorName() { return creatorName; }
    public GenderPreference getGenderPreference() { return genderPreference; }
    public GroupCategory getCategory() { return category; }
    public Integer getMinMembers() { return minMembers; }
    public Integer getMaxMembers() { return maxMembers; }
    public Integer getLikesCount() { return likesCount; }
    public GroupStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Double getDistanceKm() { return distanceKm; }
    public String getCoverImageBase64() { return coverImageBase64; }
}
