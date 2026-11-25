package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.Player;
import com.adriperez.squadmanager.model.enums.PlayerStatus;
import com.adriperez.squadmanager.model.enums.Position;

import java.time.LocalDate;

public record PlayerResponseDTO(
        Long id,
        String firstName,
        String lastName,
        Integer shirtNumber,
        LocalDate birthDate,
        Position position,
        PlayerStatus playerStatus,
        String injuryDescription,
        LocalDate injuryStartDate,
        LocalDate expectedReturnDate,
        long totalYellowCards,
        boolean warnedByCards
) {
    public static PlayerResponseDTO fromEntity(Player player) {
        return fromEntity(player, 0L);
    }

    public static PlayerResponseDTO fromEntity(Player player, long totalYellowCards) {
        boolean warned = totalYellowCards > 0 && (totalYellowCards % 5 == 4);
        return new PlayerResponseDTO(
                player.getId(),
                player.getFirstName(),
                player.getLastName(),
                player.getShirtNumber(),
                player.getBirthDate(),
                player.getPosition(),
                player.getPlayerStatus(),
                player.getInjuryDescription(),
                player.getInjuryStartDate(),
                player.getExpectedReturnDate(),
                totalYellowCards,
                warned
        );
    }
}