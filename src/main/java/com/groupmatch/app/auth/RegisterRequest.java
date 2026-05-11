package com.groupmatch.app.auth;

import com.groupmatch.app.domain.user.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class RegisterRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 80, message = "El nombre debe tener entre 2 y 80 caracteres")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotNull(message = "El género es obligatorio")
    private Gender gender;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe estar en el pasado")
    private LocalDate birthDate;

    public RegisterRequest() {}

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Gender getGender() { return gender; }
    public LocalDate getBirthDate() { return birthDate; }
}
