package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GroupEventEntity;

import java.time.LocalDateTime;

public class GroupEventResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private String location;
    private String creatorName;
    private LocalDateTime createdAt;

    public GroupEventResponse(GroupEventEntity event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.eventDate = event.getEventDate();
        this.location = event.getLocation();
        this.creatorName = event.getCreator().getName() != null
            ? event.getCreator().getName()
            : event.getCreator().getEmail();
        this.createdAt = event.getCreatedAt();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getEventDate() { return eventDate; }
    public String getLocation() { return location; }
    public String getCreatorName() { return creatorName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
