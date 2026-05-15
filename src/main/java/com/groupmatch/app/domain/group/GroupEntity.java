package com.groupmatch.app.domain.group;

import com.groupmatch.app.domain.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "groups")
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private UserEntity creator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GenderPreference genderPreference;

    @Column(nullable = false)
    private Integer minMembers;

    @Column(nullable = false)
    private Integer maxMembers;

    @Column(nullable = false)
    private Integer likesCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupStatus status = GroupStatus.OPEN;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupJoinPolicy joinPolicy = GroupJoinPolicy.OPEN;

    @Enumerated(EnumType.STRING)
    private GroupCategory category;

    private Double latitude;
    private Double longitude;

    @Column(columnDefinition = "TEXT")
    private String coverImageBase64;

    protected GroupEntity() {}

    public GroupEntity(String name, String description, UserEntity creator,
                       GenderPreference genderPreference, Integer minMembers, Integer maxMembers) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.creator = creator;
        this.genderPreference = genderPreference;
        this.minMembers = minMembers;
        this.maxMembers = maxMembers;
        this.createdAt = LocalDateTime.now();
    }

    public void update(String name, String description, GroupJoinPolicy joinPolicy) {
        this.name = name;
        this.description = description;
        if (joinPolicy != null) this.joinPolicy = joinPolicy;
    }

    public void incrementLikes() {
        this.likesCount++;
        if (this.likesCount >= this.minMembers - 1) {
            this.status = GroupStatus.ACTIVE;
        }
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public UserEntity getCreator() { return creator; }
    public GenderPreference getGenderPreference() { return genderPreference; }
    public Integer getMinMembers() { return minMembers; }
    public Integer getMaxMembers() { return maxMembers; }
    public Integer getLikesCount() { return likesCount; }
    public GroupStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public GroupJoinPolicy getJoinPolicy() { return joinPolicy; }
    public GroupCategory getCategory() { return category; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    public String getCoverImageBase64() { return coverImageBase64; }
    public void setCoverImageBase64(String coverImageBase64) { this.coverImageBase64 = coverImageBase64; }
    public void setCategory(GroupCategory category) { this.category = category; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public void setJoinPolicy(GroupJoinPolicy joinPolicy) { this.joinPolicy = joinPolicy; }
}
