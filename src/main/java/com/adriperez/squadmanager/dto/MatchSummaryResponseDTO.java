package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.Match;

import java.time.LocalDateTime;

// para el listado general
public record MatchSummaryResponseDTO(
        Long id,
        String opponent,
        LocalDateTime matchDate,
        String competition,
        Boolean homeMatch,
        Integer goalsFor,
        Integer goalsAgainst
) {
    public static MatchSummaryResponseDTO fromEntity(Match match) {
        if (match == null)
            return null;

        return new MatchSummaryResponseDTO(
                match.getId(),
                match.getOpponent(),
                match.getMatchDate(),
                match.getCompetition(),
                match.getHomeMatch(),
                match.getGoalsFor(),
                match.getGoalsAgainst()
        );
    }
}
