package com.adriperez.squadmanager.service;

import com.adriperez.squadmanager.dto.PlayerRequestDTO;
import com.adriperez.squadmanager.dto.PlayerResponseDTO;
import com.adriperez.squadmanager.exception.BusinessRuleException;
import com.adriperez.squadmanager.exception.ResourceNotFoundException;
import com.adriperez.squadmanager.model.Player;
import com.adriperez.squadmanager.model.enums.PlayerStatus;
import com.adriperez.squadmanager.model.enums.Position;
import com.adriperez.squadmanager.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;


    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional
    public PlayerResponseDTO create(PlayerRequestDTO dto){
        if (dto.shirtNumber() != null && playerRepository.existsByShirtNumber(dto.shirtNumber())) {
            throw new BusinessRuleException("El dorsal ya está asignado a otro jugador");
        }
        Player player = new Player();
        player.setFirstName(dto.firstName());
        player.setLastName(dto.lastName());
        if (dto.playerStatus() == null) {
            player.setPlayerStatus(PlayerStatus.AVAILABLE);

        } else {
            player.setPlayerStatus(dto.playerStatus());

        }
        player.setPosition(dto.position());
        player.setBirthDate(dto.birthDate());
        player.setShirtNumber(dto.shirtNumber());

        Player saved = playerRepository.save(player);

        return PlayerResponseDTO.fromEntity(saved);
    }

    public List<PlayerResponseDTO> findAll(Position position, PlayerStatus status) {
        List<Player> players;

        if (position != null && status != null) {
            players = playerRepository.findByPositionAndPlayerStatus(position, status);
        } else if (position != null) {
            players = playerRepository.findByPosition(position);
        } else if (status != null) {
            players = playerRepository.findByPlayerStatus(status);
        } else {
            players = playerRepository.findAll();
        }

        return players.stream()
                .map(PlayerResponseDTO::fromEntity)
                .toList();
    }

    public PlayerResponseDTO findById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con ID: " + id));
        return PlayerResponseDTO.fromEntity(player);
    }

    @Transactional
    public PlayerResponseDTO update(Long id, PlayerRequestDTO dto) {
        Player existingPlayer = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con ID: " + id));

        // Validar dorsal solo si lo está cambiando por uno que ya tiene OTRO jugador
        if (dto.shirtNumber() != null && playerRepository.existsByShirtNumberAndIdNot(dto.shirtNumber(), id)) {
            throw new BusinessRuleException("El dorsal " + dto.shirtNumber() + " ya está asignado a otro jugador");
        }

        mapDtoToEntity(dto, existingPlayer);

        Player updated = playerRepository.save(existingPlayer);
        return PlayerResponseDTO.fromEntity(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Jugador no encontrado con ID: " + id);
        }
        playerRepository.deleteById(id);
    }

    // Método auxiliar privado para no repetir código entre create y update
    private void mapDtoToEntity(PlayerRequestDTO dto, Player player) {
        player.setFirstName(dto.firstName());
        player.setLastName(dto.lastName());
        player.setShirtNumber(dto.shirtNumber());
        player.setBirthDate(dto.birthDate());
        player.setPosition(dto.position());
        player.setPlayerStatus(dto.playerStatus() != null ? dto.playerStatus() : PlayerStatus.AVAILABLE);
    }

}
