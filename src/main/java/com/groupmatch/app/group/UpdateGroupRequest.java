package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GroupJoinPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateGroupRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
    private String description;

    private GroupJoinPolicy joinPolicy;

    public UpdateGroupRequest() {}

    public String getName() { return name; }
    public String getDescription() { return description; }
    public GroupJoinPolicy getJoinPolicy() { return joinPolicy; }
}
