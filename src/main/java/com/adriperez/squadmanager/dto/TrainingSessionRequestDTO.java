package com.adriperez.squadmanager.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record TrainingSessionRequestDTO(
        @NotNull(message = "La fecha de la sesión es obligatoria")
        LocalDateTime sessionDate,

        @NotBlank(message = "El objetivo de la sesión es obligatorio")
        String focus,

        @NotNull(message = "La duración es obligatoria")
        @Min(value = 15, message = "La duración mínima es de 15 minutos")
        @Max(value = 240, message = "La duración máxima es de 240 minutos")
        Integer durationMinutes,

        String notes
) {}