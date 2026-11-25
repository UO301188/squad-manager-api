// MatchEventRequestDTO.java
package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.enums.MatchEventType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MatchEventRequestDTO(
        @NotNull(message = "El jugador es obligatorio")
        Long playerId,

        @NotNull(message = "El tipo de evento es obligatorio")
        MatchEventType eventType,

        @NotNull(message = "El minuto es obligatorio")
        @Min(value = 1, message = "El minuto mínimo es 1")
        @Max(value = 130, message = "El minuto máximo es 130")
        Integer minute,

        String notes
) {}