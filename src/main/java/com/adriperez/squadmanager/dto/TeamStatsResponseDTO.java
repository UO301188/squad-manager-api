package com.adriperez.squadmanager.dto;

import java.util.List;

public record TeamStatsResponseDTO(
        int totalPlayed,
        int wins,
        int draws,
        int losses,
        int points,
        double winPercentage,
        int goalsFor,
        int goalsAgainst,
        int goalDifference,
        double avgGoalsFor,
        double avgGoalsAgainst,
        int cleanSheets,
        List<RecentMatchFormDTO> recentForm,
        VenueSplitDTO homeStats,
        VenueSplitDTO awayStats
) {
    public record RecentMatchFormDTO(
            Long matchId,
            String opponent,
            boolean homeMatch,
            int goalsFor,
            int goalsAgainst,
            String result
    ) {}

    public record VenueSplitDTO(
            int played,
            int wins,
            int draws,
            int losses,
            int goalsFor,
            int goalsAgainst,
            int points
    ) {}
}