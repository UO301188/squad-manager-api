package com.adriperez.squadmanager.dto;

public record PlayerMatchStatsDTO(
        Long playerId,
        Long matchesCalledUp,
        Long matchesPlayed,
        Long totalMinutes,
        Long totalGoals,
        Long totalAssists,
        Long yellowCards,
        Long redCards
) {}