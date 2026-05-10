package com.groupmatch.app.group;

import com.groupmatch.app.domain.group.GenderPreference;
import com.groupmatch.app.domain.group.GroupCategory;
import jakarta.validation.constraints.*;

public class CreateGroupRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String name;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres")
    private String description;

    @NotNull(message = "La preferencia de género es obligatoria")
    private GenderPreference genderPreference;

    @NotNull(message = "La categoría es obligatoria")
    private GroupCategory category;

    @NotNull(message = "El mínimo de miembros es obligatorio")
    @Min(value = 2, message = "El mínimo de miembros debe ser al menos 2")
    @Max(value = 50, message = "El mínimo de miembros no puede superar 50")
    private Integer minMembers;

    @NotNull(message = "El máximo de miembros es obligatorio")
    @Min(value = 2, message = "El máximo de miembros debe ser al menos 2")
    @Max(value = 100, message = "El máximo de miembros no puede superar 100")
    private Integer maxMembers;

    @DecimalMin(value = "-90.0", message = "Latitud inválida")
    @DecimalMax(value = "90.0", message = "Latitud inválida")
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitud inválida")
    @DecimalMax(value = "180.0", message = "Longitud inválida")
    private Double longitude;

    public CreateGroupRequest() {}

    public String getName() { return name; }
    public String getDescription() { return description; }
    public GenderPreference getGenderPreference() { return genderPreference; }
    public GroupCategory getCategory() { return category; }
    public Integer getMinMembers() { return minMembers; }
    public Integer getMaxMembers() { return maxMembers; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
}
