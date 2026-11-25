package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.Match;

import java.time.LocalDateTime;
import java.util.List;

public record MatchDetailResponseDTO(
        Long id,
        String opponent,
        LocalDateTime matchDate,
        String competition,
        Boolean homeMatch,
        Integer goalsFor,
        Integer goalsAgainst,
        String formation,
        List<MatchAppearanceResponseDTO> appearances
) {
    public static MatchDetailResponseDTO fromEntity(Match match) {
        if (match == null) {
            return null;
        }
        return new MatchDetailResponseDTO(
                match.getId(),
                match.getOpponent(),
                match.getMatchDate(),
                match.getCompetition(),
                match.getHomeMatch(),
                match.getGoalsFor(),
                match.getGoalsAgainst(),
                match.getFormation() != null ? match.getFormation() : "4-3-3",
                match.getMatchAppearances().stream().map(MatchAppearanceResponseDTO::fromEntity).toList()
        );
    }
}