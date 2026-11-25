package com.adriperez.squadmanager.service;

import com.adriperez.squadmanager.dto.PlayerRequestDTO;
import com.adriperez.squadmanager.dto.PlayerResponseDTO;
import com.adriperez.squadmanager.exception.BusinessRuleException;
import com.adriperez.squadmanager.exception.ResourceNotFoundException;
import com.adriperez.squadmanager.model.AppUser;
import com.adriperez.squadmanager.model.Player;
import com.adriperez.squadmanager.model.enums.PlayerStatus;
import com.adriperez.squadmanager.model.enums.Position;
import com.adriperez.squadmanager.repository.MatchAppearanceRepository;
import com.adriperez.squadmanager.repository.PlayerRepository;
import com.adriperez.squadmanager.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final MatchAppearanceRepository matchAppearanceRepository;
    private final CurrentUserService currentUserService;

    public PlayerService(
            PlayerRepository playerRepository,
            MatchAppearanceRepository matchAppearanceRepository,
            CurrentUserService currentUserService) {
        this.playerRepository = playerRepository;
        this.matchAppearanceRepository = matchAppearanceRepository;
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
        applyDtoToPlayer(player, dto);

        Player saved = playerRepository.save(player);
        return PlayerResponseDTO.fromEntity(saved, 0L);
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

        Map<Long, Long> yellowsMap = getYellowCardsMap(userId);

        return players.stream()
                .map(p -> PlayerResponseDTO.fromEntity(p, yellowsMap.getOrDefault(p.getId(), 0L)))
                .toList();
    }

    public PlayerResponseDTO findById(Long id) {
        Long userId = currentUserService.getCurrentUser().getId();
        Player player = playerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));
        long yellows = matchAppearanceRepository.getTotalYellowCardsByPlayerId(id);
        return PlayerResponseDTO.fromEntity(player, yellows);
    }

    @Transactional
    public PlayerResponseDTO update(Long id, PlayerRequestDTO dto) {
        Long userId = currentUserService.getCurrentUser().getId();
        Player player = playerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));

        if (dto.shirtNumber() != null && playerRepository.existsByUserIdAndShirtNumberAndIdNot(userId, dto.shirtNumber(), id)) {
            throw new BusinessRuleException("El dorsal " + dto.shirtNumber() + " ya está asignado a otro jugador de tu plantilla");
        }

        applyDtoToPlayer(player, dto);

        Player updated = playerRepository.save(player);
        long yellows = matchAppearanceRepository.getTotalYellowCardsByPlayerId(id);
        return PlayerResponseDTO.fromEntity(updated, yellows);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = currentUserService.getCurrentUser().getId();
        Player player = playerRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));
        playerRepository.delete(player);
    }

    private void applyDtoToPlayer(Player player, PlayerRequestDTO dto) {
        player.setFirstName(dto.firstName());
        player.setLastName(dto.lastName());
        player.setShirtNumber(dto.shirtNumber());
        player.setBirthDate(dto.birthDate());
        player.setPosition(dto.position());

        PlayerStatus targetStatus = dto.playerStatus() != null ? dto.playerStatus() : PlayerStatus.AVAILABLE;
        player.setPlayerStatus(targetStatus);

        if (targetStatus == PlayerStatus.INJURED) {
            player.setInjuryDescription(dto.injuryDescription() != null && !dto.injuryDescription().isBlank()
                    ? dto.injuryDescription().trim()
                    : "Lesión muscular / en evaluación");
            player.setInjuryStartDate(dto.injuryStartDate() != null ? dto.injuryStartDate() : LocalDate.now());
            player.setExpectedReturnDate(dto.expectedReturnDate());
        } else {
            player.setInjuryDescription(null);
            player.setInjuryStartDate(null);
            player.setExpectedReturnDate(null);
        }
    }

    private Map<Long, Long> getYellowCardsMap(Long userId) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : matchAppearanceRepository.getYellowCardsCountByUserId(userId)) {
            Long playerId = ((Number) row[0]).longValue();
            Long count = ((Number) row[1]).longValue();
            map.put(playerId, count);
        }
        return map;
    }
}