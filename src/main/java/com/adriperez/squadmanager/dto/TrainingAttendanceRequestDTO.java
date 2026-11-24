package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.enums.AttendanceStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TrainingAttendanceRequestDTO(
        @NotNull(message = "El ID del jugador es obligatorio")
        Long playerId,

        @NotNull(message = "El estado de asistencia es obligatorio")
        AttendanceStatus attendanceStatus,

        String absenceReason,

        @Min(value = 1, message = "El RPE mínimo es 1")
        @Max(value = 10, message = "El RPE máximo es 10")
        Integer rpe
) {}