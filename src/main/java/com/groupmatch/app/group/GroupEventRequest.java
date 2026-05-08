package com.groupmatch.app.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class GroupEventRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 100, message = "El título no puede superar 100 caracteres")
    private String title;

    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    private String description;

    @NotNull(message = "La fecha del evento es obligatoria")
    private LocalDateTime eventDate;

    @Size(max = 200, message = "La ubicación no puede superar 200 caracteres")
    private String location;

    public GroupEventRequest() {}

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDateTime getEventDate() { return eventDate; }
    public String getLocation() { return location; }
}
