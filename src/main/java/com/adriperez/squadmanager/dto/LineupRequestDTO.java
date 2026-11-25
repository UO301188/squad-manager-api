package com.adriperez.squadmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record LineupRequestDTO(
        @NotBlank(message = "El sistema táctico es obligatorio")
        String formation,

        @NotNull(message = "La distribución de posiciones es obligatoria")
        Map<Integer, Long> slots
) {}