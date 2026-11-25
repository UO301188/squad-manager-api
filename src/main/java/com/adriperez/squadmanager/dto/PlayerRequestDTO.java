package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.enums.PlayerStatus;
import com.adriperez.squadmanager.model.enums.Position;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PlayerRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        String firstName,

        @NotBlank(message = "Los apellidos son obligatorios")
        String lastName,

        @NotNull(message = "El dorsal es obligatorio")
        @Min(value = 1, message = "El dorsal mínimo es 1")
        @Max(value = 99, message = "El dorsal máximo es 99")
        Integer shirtNumber,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate birthDate,

        @NotNull(message = "La demarcación es obligatoria")
        Position position,

        PlayerStatus playerStatus,

        String injuryDescription,
        LocalDate injuryStartDate,
        LocalDate expectedReturnDate
) {}