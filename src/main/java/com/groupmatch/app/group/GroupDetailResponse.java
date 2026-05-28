package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GenderPreference;
import com.groupmatch.app.domain.group.GroupEntity;
import com.groupmatch.app.domain.group.GroupMemberEntity;
import com.groupmatch.app.domain.group.GroupStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class GroupDetailResponse {

    private Long id;
    private UUID uuid;
    private String name;
    private String description;
    private String creatorName;
    private GenderPreference genderPreference;
    private Integer minMembers;
    private Integer maxMembers;
    private Integer likesCount;
    private GroupStatus status;
    private LocalDateTime createdAt;
    private List<MemberResponse> members;
    private String coverImageBase64;

    public GroupDetailResponse(GroupEntity group, List<GroupMemberEntity> members) {
        this.id = group.getId();
        this.uuid = group.getUuid();
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
        this.members = members.stream()
            .map(MemberResponse::new)
            .toList();
        this.coverImageBase64 = group.getCoverImageBase64();
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCreatorName() { return creatorName; }
    public GenderPreference getGenderPreference() { return genderPreference; }
    public Integer getMinMembers() { return minMembers; }
    public Integer getMaxMembers() { return maxMembers; }
    public Integer getLikesCount() { return likesCount; }
    public GroupStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<MemberResponse> getMembers() { return members; }
    public String getCoverImageBase64() { return coverImageBase64; }
}
