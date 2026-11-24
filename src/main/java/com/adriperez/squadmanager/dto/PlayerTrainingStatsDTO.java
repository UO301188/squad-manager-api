package com.adriperez.squadmanager.dto;

public record PlayerTrainingStatsDTO(
        Long playerId,
        Long totalSessions,
        Long sessionsPresent,
        Double averageRpe
) {}