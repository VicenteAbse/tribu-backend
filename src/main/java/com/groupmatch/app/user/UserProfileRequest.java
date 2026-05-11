package com.groupmatch.app.user;

import jakarta.validation.constraints.*;

public class UserProfileRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
    private String name;

    @Size(max = 300, message = "La descripción no puede superar 300 caracteres")
    private String description;

    @NotNull(message = "El radio de búsqueda es obligatorio")
    @Min(value = 1, message = "El radio de búsqueda debe ser al menos 1 km")
    @Max(value = 500, message = "El radio de búsqueda no puede superar 500 km")
    private Integer searchRadiusKm;

    public UserProfileRequest() {}

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getSearchRadiusKm() { return searchRadiusKm; }
}
