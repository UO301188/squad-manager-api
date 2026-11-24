package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.MatchAppearance;
import com.adriperez.squadmanager.model.enums.CallUpStatus;
import com.adriperez.squadmanager.model.enums.Position;

public record MatchAppearanceResponseDTO(Long id,
    Long playerId,
    String playerFullName,
    Integer shirtNumber,
    Position position,
    CallUpStatus callUpStatus,
    Integer minutesPlayed,
    Integer goals,
    Integer assists,
    Integer yellowCards,
    boolean redCard)  {
    public static MatchAppearanceResponseDTO fromEntity(MatchAppearance appearance) {
        if (appearance == null) {
            return null;
        }
        String playerFullName = appearance.getPlayer().getFirstName() + " " + appearance.getPlayer().getLastName();
        return new MatchAppearanceResponseDTO(
                appearance.getId(),
                appearance.getPlayer().getId(),
                playerFullName,
                appearance.getPlayer().getShirtNumber(),
                appearance.getPlayer().getPosition(),
                appearance.getCallUpStatus(),
                appearance.getMinutesPlayed(),
                appearance.getGoals(),
                appearance.getAssists(),
                appearance.getYellowCards(),
                appearance.isRedCard()
        );
    }
}
