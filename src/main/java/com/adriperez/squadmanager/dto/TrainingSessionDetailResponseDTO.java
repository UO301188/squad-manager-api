package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.TrainingSession;

import java.time.LocalDateTime;
import java.util.List;

public record TrainingSessionDetailResponseDTO(
        Long id,
        LocalDateTime sessionDate,
        String focus,
        Integer durationMinutes,
        String notes,
        List<TrainingAttendanceResponseDTO> attendances
) {
    public static TrainingSessionDetailResponseDTO fromEntity(TrainingSession session) {
        List<TrainingAttendanceResponseDTO> attendanceDTOs = session.getTrainingAttendances()
                .stream()
                .map(TrainingAttendanceResponseDTO::fromEntity)
                .toList();

        return new TrainingSessionDetailResponseDTO(
                session.getId(),
                session.getSessionDate(),
                session.getFocus(),
                session.getDurationMinutes(),
                session.getNotes(),
                attendanceDTOs
        );
    }
}