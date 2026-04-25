package com.groupmatch.app.user;

import com.groupmatch.app.domain.user.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class UserProfileRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
    private String name;

    @NotNull(message = "El género es obligatorio")
    private Gender gender;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe estar en el pasado")
    private LocalDate birthDate;

    @NotNull(message = "El radio de búsqueda es obligatorio")
    @Min(value = 1, message = "El radio de búsqueda debe ser al menos 1 km")
    @Max(value = 500, message = "El radio de búsqueda no puede superar 500 km")
    private Integer searchRadiusKm;

    public UserProfileRequest() {}

    public String getName() { return name; }
    public Gender getGender() { return gender; }
    public LocalDate getBirthDate() { return birthDate; }
    public Integer getSearchRadiusKm() { return searchRadiusKm; }
}
