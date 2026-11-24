package com.adriperez.squadmanager.service;

import com.adriperez.squadmanager.dto.PlayerRequestDTO;
import com.adriperez.squadmanager.dto.PlayerResponseDTO;
import com.adriperez.squadmanager.exception.BusinessRuleException;
import com.adriperez.squadmanager.exception.ResourceNotFoundException;
import com.adriperez.squadmanager.model.AppUser;
import com.adriperez.squadmanager.model.Player;
import com.adriperez.squadmanager.model.enums.PlayerStatus;
import com.adriperez.squadmanager.model.enums.Position;
import com.adriperez.squadmanager.repository.PlayerRepository;
import com.adriperez.squadmanager.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final CurrentUserService currentUserService;

    public PlayerService(PlayerRepository playerRepository, CurrentUserService currentUserService) {
        this.playerRepository = playerRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public PlayerResponseDTO create(PlayerRequestDTO dto) {
        AppUser currentUser = currentUserService.getCurrentUser();
        Long userId = currentUser.getId();

        if (dto.shirtNumber() != null && playerRepository.existsByUserIdAndShirtNumber(userId, dto.shirtNumber())) {
            throw new BusinessRuleException("El dorsal " + dto.shirtNumber() + " ya está asignado a otro jugador de tu plantilla");
        }

        Player player = new Player();
        player.setUser(currentUser);
        player.setFirstName(dto.firstName());
        player.setLastName(dto.lastName());
        player.setShirtNumber(dto.shirtNumber());
        player.setBirthDate(dto.birthDate());
        player.setPosition(dto.position());
        player.setPlayerStatus(dto.playerStatus() != null ? dto.playerStatus() : PlayerStatus.AVAILABLE);

        Player saved = playerRepository.save(player);
        return PlayerResponseDTO.fromEntity(saved);
    }

    public List<PlayerResponseDTO> findAll(Position position, PlayerStatus status) {
        Long userId = currentUserService.getCurrentUser().getId();
        List<Player> players;

        if (position != null && status != null) {
            players = playerRepository.findByUserIdAndPositionAndPlayerStatus(userId, position, status);
        } else if (position != null) {
            players = playerRepository.findByUserIdAndPosition(userId, position);
        } else if (status != null) {
            players = playerRepository.findByUserIdAndPlayerStatus(userId, status);
        } else {
            players = playerRepository.findByUserId(userId);
        }

        return players.stream()
                .map(PlayerResponseDTO::fromEntity)
                .toList();
    }

    public PlayerResponseDTO findById(Long id) {
        Long userId = currentUserService.getCurrentUser().getId();
        Player player = playerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));
        return PlayerResponseDTO.fromEntity(player);
    }

    @Transactional
    public PlayerResponseDTO update(Long id, PlayerRequestDTO dto) {
        Long userId = currentUserService.getCurrentUser().getId();
        Player player = playerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));

        if (dto.shirtNumber() != null && playerRepository.existsByUserIdAndShirtNumberAndIdNot(userId, dto.shirtNumber(), id)) {
            throw new BusinessRuleException("El dorsal " + dto.shirtNumber() + " ya está asignado a otro jugador de tu plantilla");
        }

        player.setFirstName(dto.firstName());
        player.setLastName(dto.lastName());
        player.setShirtNumber(dto.shirtNumber());
        player.setBirthDate(dto.birthDate());
        player.setPosition(dto.position());
        if (dto.playerStatus() != null) {
            player.setPlayerStatus(dto.playerStatus());
        }

        Player updated = playerRepository.save(player);
        return PlayerResponseDTO.fromEntity(updated);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = currentUserService.getCurrentUser().getId();
        Player player = playerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));
        playerRepository.delete(player);
    }
}