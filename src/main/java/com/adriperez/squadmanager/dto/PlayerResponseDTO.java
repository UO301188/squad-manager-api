package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.Player;
import com.adriperez.squadmanager.model.enums.PlayerStatus;
import com.adriperez.squadmanager.model.enums.Position;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PlayerResponseDTO(
        Long id,
        String firstName,
        String lastName,
        Integer shirtNumber,
        LocalDate birthDate,
        Position position,
        PlayerStatus playerStatus
) {
    /**
     * Método estático de fábrica para convertir una entidad Player a PlayerResponseDTO.
     */
    public static PlayerResponseDTO fromEntity(Player player) {
        if (player == null) {
            return null;
        }
        return new PlayerResponseDTO(
                player.getId(),
                player.getFirstName(),
                player.getLastName(),
                player.getShirtNumber(),
                player.getBirthDate(),
                player.getPosition(),
                player.getPlayerStatus()
        );
    }
}
