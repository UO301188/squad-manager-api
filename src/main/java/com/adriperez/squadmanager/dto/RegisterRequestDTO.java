package com.adriperez.squadmanager.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "El nombre del equipo es obligatorio") String clubName,
        @NotBlank(message = "El email es obligatorio") @Email(message = "Email inválido") String email,
        @NotBlank(message = "La contraseña es obligatoria") @Size(min = 6, message = "Mínimo 6 caracteres") String password
) {}