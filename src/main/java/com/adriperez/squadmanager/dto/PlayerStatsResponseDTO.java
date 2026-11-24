package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.enums.PlayerStatus;
import com.adriperez.squadmanager.model.enums.Position;

public record PlayerStatsResponseDTO(
        Long playerId,
        String fullName,
        Integer shirtNumber,
        Position position,
        PlayerStatus playerStatus,
        long matchesCalledUp,
        long matchesPlayed,
        long totalMinutes,
        long totalGoals,
        long totalAssists,
        long goalContributions,
        double goalsPer90,
        long yellowCards,
        long redCards,
        long totalSessions,
        long sessionsPresent,
        double attendancePercentage,
        double averageRpe
) {}