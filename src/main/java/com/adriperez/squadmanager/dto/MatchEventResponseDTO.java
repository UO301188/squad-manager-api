// MatchEventResponseDTO.java
package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.MatchEvent;
import com.adriperez.squadmanager.model.enums.MatchEventType;

public record MatchEventResponseDTO(
        Long id,
        Long playerId,
        String playerFullName,
        Integer shirtNumber,
        MatchEventType eventType,
        Integer minute,
        String notes
) {
    public static MatchEventResponseDTO fromEntity(MatchEvent event) {
        return new MatchEventResponseDTO(
                event.getId(),
                event.getPlayer().getId(),
                event.getPlayer().getFirstName() + " " + event.getPlayer().getLastName(),
                event.getPlayer().getShirtNumber(),
                event.getEventType(),
                event.getMinute(),
                event.getNotes()
        );
    }
}