package com.groupmatch.app.domain.group;

import com.groupmatch.app.domain.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_events")
public class GroupEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private UserEntity creator;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    private String location;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected GroupEventEntity() {}

    public GroupEventEntity(GroupEntity group, UserEntity creator, String title,
                            String description, LocalDateTime eventDate, String location) {
        this.group = group;
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.eventDate = eventDate;
        this.location = location;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public GroupEntity getGroup() { return group; }
    public UserEntity getCreator() { return creator; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getEventDate() { return eventDate; }
    public String getLocation() { return location; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
