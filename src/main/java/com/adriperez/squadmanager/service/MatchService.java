package com.adriperez.squadmanager.service;

import com.adriperez.squadmanager.dto.*;
import com.adriperez.squadmanager.exception.BusinessRuleException;
import com.adriperez.squadmanager.exception.ResourceNotFoundException;
import com.adriperez.squadmanager.model.AppUser;
import com.adriperez.squadmanager.model.Match;
import com.adriperez.squadmanager.model.MatchAppearance;
import com.adriperez.squadmanager.model.Player;
import com.adriperez.squadmanager.model.enums.CallUpStatus;
import com.adriperez.squadmanager.model.enums.PlayerStatus;
import com.adriperez.squadmanager.repository.MatchAppearanceRepository;
import com.adriperez.squadmanager.repository.MatchRepository;
import com.adriperez.squadmanager.repository.PlayerRepository;
import com.adriperez.squadmanager.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchAppearanceRepository matchAppearanceRepository;
    private final PlayerRepository playerRepository;
    private final CurrentUserService currentUserService;

    public MatchService(
            MatchRepository matchRepository,
            MatchAppearanceRepository matchAppearanceRepository,
            PlayerRepository playerRepository,
            CurrentUserService currentUserService) {
        this.matchRepository = matchRepository;
        this.matchAppearanceRepository = matchAppearanceRepository;
        this.playerRepository = playerRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public MatchSummaryResponseDTO createMatch(MatchRequestDTO dto) {
        AppUser currentUser = currentUserService.getCurrentUser();

        Match match = new Match();
        match.setUser(currentUser);
        match.setOpponent(dto.opponent);
        match.setMatchDate(dto.matchDate);
        match.setCompetition(dto.competition);
        match.setHomeMatch(dto.homeMatch);

        Match saved = matchRepository.save(match);
        return MatchSummaryResponseDTO.fromEntity(saved);
    }

    public List<MatchSummaryResponseDTO> getAllMatches() {
        Long userId = currentUserService.getCurrentUser().getId();
        return matchRepository.findByUserIdOrderByMatchDateAsc(userId)
                .stream()
                .map(MatchSummaryResponseDTO::fromEntity)
                .toList();
    }

    public MatchDetailResponseDTO getMatchById(Long id) {
        Long userId = currentUserService.getCurrentUser().getId();
        Match match = matchRepository.findByIdAndUserIdWithAppearances(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
        return MatchDetailResponseDTO.fromEntity(match);
    }

    @Transactional
    public MatchSummaryResponseDTO updateMatchResult(Long id, MatchResultRequestDTO dto) {
        Long userId = currentUserService.getCurrentUser().getId();
        Match currentMatch = matchRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));

        currentMatch.setGoalsFor(dto.goalsFor);
        currentMatch.setGoalsAgainst(dto.goalsAgainst);

        Match saved = matchRepository.save(currentMatch);
        return MatchSummaryResponseDTO.fromEntity(saved);
    }

    @Transactional
    public MatchAppearanceResponseDTO addPlayerToMatch(Long matchId, CallUpRequestDTO dto) {
        Long userId = currentUserService.getCurrentUser().getId();

        Match match = matchRepository.findByIdAndUserId(matchId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + matchId));

        Player player = playerRepository.findByIdAndUserId(dto.playerId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + dto.playerId));

        if (matchAppearanceRepository.existsByMatchIdAndPlayerId(matchId, dto.playerId)) {
            throw new BusinessRuleException("El jugador ya está registrado en este partido");
        }

        if (dto.callUpStatus != CallUpStatus.NOT_CALLED) {
            if (player.getPlayerStatus() != PlayerStatus.AVAILABLE) {
                throw new BusinessRuleException("No se puede convocar a un jugador lesionado o sancionado");
            }

            if (dto.callUpStatus == CallUpStatus.STARTER) {
                long currentStarters = matchAppearanceRepository.countByMatchIdAndCallUpStatus(matchId, CallUpStatus.STARTER);
                if (currentStarters >= 11) {
                    throw new BusinessRuleException("Ya hay 11 titulares en la convocatoria");
                }
            }
        }

        MatchAppearance appearance = new MatchAppearance();
        appearance.setMatch(match);
        appearance.setPlayer(player);
        appearance.setCallUpStatus(dto.callUpStatus);
        appearance.setMinutesPlayed(0);
        appearance.setGoals(0);
        appearance.setAssists(0);
        appearance.setYellowCards(0);
        appearance.setRedCard(false);

        MatchAppearance savedAppearance = matchAppearanceRepository.save(appearance);
        return MatchAppearanceResponseDTO.fromEntity(savedAppearance);
    }

    @Transactional
    public MatchAppearanceResponseDTO updatePlayerAppearanceStats(Long matchId, Long playerId, MatchAppearanceStatsRequestDTO dto) {
        Long userId = currentUserService.getCurrentUser().getId();

        if (matchRepository.findByIdAndUserId(matchId, userId).isEmpty()) {
            throw new ResourceNotFoundException("Partido no encontrado con id: " + matchId);
        }

        MatchAppearance appearance = matchAppearanceRepository.findByMatchIdAndPlayerId(matchId, playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró participación para el jugador " + playerId + " en el partido " + matchId));

        if (dto.callUpStatus == CallUpStatus.NOT_CALLED && dto.minutesPlayed > 0) {
            throw new BusinessRuleException("Un jugador no convocado no puede tener minutos jugados");
        }

        if (dto.minutesPlayed == 0 && (dto.goals > 0 || dto.assists > 0 || dto.yellowCards > 0 || dto.redCard)) {
            throw new BusinessRuleException("Un jugador con 0 minutos no puede registrar goles, asistencias ni tarjetas");
        }

        if (dto.callUpStatus == CallUpStatus.STARTER && appearance.getCallUpStatus() != CallUpStatus.STARTER) {
            long currentStarters = matchAppearanceRepository.countByMatchIdAndCallUpStatus(matchId, CallUpStatus.STARTER);
            if (currentStarters >= 11) {
                throw new BusinessRuleException("Ya hay 11 titulares en la convocatoria");
            }
        }

        appearance.setCallUpStatus(dto.callUpStatus);
        appearance.setMinutesPlayed(dto.minutesPlayed);
        appearance.setGoals(dto.goals);
        appearance.setAssists(dto.assists);
        appearance.setYellowCards(dto.yellowCards);
        appearance.setRedCard(dto.redCard);

        MatchAppearance updatedAppearance = matchAppearanceRepository.save(appearance);
        return MatchAppearanceResponseDTO.fromEntity(updatedAppearance);
    }

    @Transactional
    public void removePlayerFromMatch(Long matchId, Long playerId) {
        Long userId = currentUserService.getCurrentUser().getId();

        if (matchRepository.findByIdAndUserId(matchId, userId).isEmpty()) {
            throw new ResourceNotFoundException("Partido no encontrado con id: " + matchId);
        }

        MatchAppearance appearance = matchAppearanceRepository.findByMatchIdAndPlayerId(matchId, playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El jugador " + playerId + " no está registrado en el partido " + matchId));

        matchAppearanceRepository.delete(appearance);
    }
}