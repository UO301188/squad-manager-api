// PlayerMatchStatsDTO.java
package com.adriperez.squadmanager.dto;

public record PlayerMatchStatsDTO(
        Long playerId,
        Long matchesCalledUp,
        Long matchesPlayed,
        Long starts,
        Long subAppearances,
        Long totalMinutes,
        Long totalGoals,
        Long totalAssists,
        Long cleanSheets,
        Long matchesWon,
        Long matchesFinishedPlayed,
        Long yellowCards,
        Long redCards,
        Double averageRating
) {}