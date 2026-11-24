package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.TrainingSession;

import java.time.LocalDateTime;

public record TrainingSessionSummaryResponseDTO(
        Long id,
        LocalDateTime sessionDate,
        String focus,
        Integer durationMinutes,
        String notes
) {
    public static TrainingSessionSummaryResponseDTO fromEntity(TrainingSession session) {
        return new TrainingSessionSummaryResponseDTO(
                session.getId(),
                session.getSessionDate(),
                session.getFocus(),
                session.getDurationMinutes(),
                session.getNotes()
        );
    }
}