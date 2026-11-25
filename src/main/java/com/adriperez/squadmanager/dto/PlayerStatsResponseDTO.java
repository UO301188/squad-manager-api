// PlayerStatsResponseDTO.java
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
        long starts,
        long subAppearances,
        long totalMinutes,
        double avgMinutesPerMatch,
        long totalGoals,
        long totalAssists,
        long goalContributions,
        double goalsPer90,
        double assistsPer90,
        double goalContributionsPer90,
        long cleanSheets,
        double winRatePercentage,
        long yellowCards,
        long redCards,
        boolean warnedByCards,
        double averageRating,
        long totalSessions,
        long sessionsPresent,
        double attendancePercentage,
        double averageRpe
) {}