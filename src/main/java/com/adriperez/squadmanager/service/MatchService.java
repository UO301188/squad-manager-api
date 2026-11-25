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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MatchService {

    private static final Set<String> VALID_FORMATIONS = Set.of("4-3-3", "4-4-2", "4-2-3-1", "3-5-2");

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
        match.setFormation("4-3-3");

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
    public MatchDetailResponseDTO updateLineup(Long matchId, LineupRequestDTO dto) {
        Long userId = currentUserService.getCurrentUser().getId();

        if (!VALID_FORMATIONS.contains(dto.formation())) {
            throw new BusinessRuleException("Sistema táctico no soportado: " + dto.formation());
        }

        Map<Integer, Long> slots = dto.slots();
        if (slots.size() > 11) {
            throw new BusinessRuleException("Un once inicial no puede superar los 11 jugadores");
        }

        for (Integer slot : slots.keySet()) {
            if (slot == null || slot < 1 || slot > 11) {
                throw new BusinessRuleException("Las posiciones tácticas deben estar numeradas del 1 al 11");
            }
        }

        if (new HashSet<>(slots.values()).size() != slots.size()) {
            throw new BusinessRuleException("No puedes colocar al mismo jugador en dos posiciones de la pizarra");
        }

        Match match = matchRepository.findByIdAndUserIdWithAppearances(matchId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + matchId));

        Map<Long, MatchAppearance> appearancesByPlayer = match.getMatchAppearances()
                .stream()
                .collect(Collectors.toMap(a -> a.getPlayer().getId(), Function.identity()));

        Map<Long, Integer> slotByPlayerId = new HashMap<>();
        for (Map.Entry<Integer, Long> entry : slots.entrySet()) {
            Long playerId = entry.getValue();
            MatchAppearance appearance = appearancesByPlayer.get(playerId);
            if (appearance == null) {
                throw new BusinessRuleException("Todos los jugadores de la pizarra deben estar primero en la convocatoria");
            }
            if (appearance.getCallUpStatus() == CallUpStatus.NOT_CALLED
                    && appearance.getPlayer().getPlayerStatus() != PlayerStatus.AVAILABLE) {
                throw new BusinessRuleException("No se puede alinear a un jugador lesionado o sancionado");
            }
            slotByPlayerId.put(playerId, entry.getKey());
        }

        match.setFormation(dto.formation());

        for (MatchAppearance appearance : match.getMatchAppearances()) {
            Long pId = appearance.getPlayer().getId();
            if (slotByPlayerId.containsKey(pId)) {
                appearance.setCallUpStatus(CallUpStatus.STARTER);
                appearance.setLineupSlot(slotByPlayerId.get(pId));
            } else {
                if (appearance.getCallUpStatus() == CallUpStatus.STARTER) {
                    appearance.setCallUpStatus(CallUpStatus.SUBSTITUTE);
                }
                appearance.setLineupSlot(null);
            }
        }

        Match saved = matchRepository.save(match);
        return MatchDetailResponseDTO.fromEntity(saved);
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
            if (player.getPlayerStatus() == PlayerStatus.INJURED) {
                String detail = player.getInjuryDescription() != null ? " (" + player.getInjuryDescription() + ")" : "";
                throw new BusinessRuleException("No se puede convocar a un jugador lesionado" + detail);
            }
            if (player.getPlayerStatus() == PlayerStatus.SUSPENDED) {
                throw new BusinessRuleException("No se puede convocar a un jugador sancionado por acumulación o tarjeta roja");
            }
            if (player.getPlayerStatus() != PlayerStatus.AVAILABLE) {
                throw new BusinessRuleException("El jugador no se encuentra disponible para competir");
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

        int previousYellowsInMatch = appearance.getYellowCards() != null ? appearance.getYellowCards() : 0;
        long totalYellowsBefore = matchAppearanceRepository.getTotalYellowCardsByPlayerId(playerId);

        appearance.setCallUpStatus(dto.callUpStatus);
        if (dto.callUpStatus != CallUpStatus.STARTER) {
            appearance.setLineupSlot(null);
        }
        appearance.setMinutesPlayed(dto.minutesPlayed);
        appearance.setGoals(dto.goals);
        appearance.setAssists(dto.assists);
        appearance.setYellowCards(dto.yellowCards);
        appearance.setRedCard(dto.redCard);

        MatchAppearance updatedAppearance = matchAppearanceRepository.save(appearance);

        // Regla disciplinaria automática: Roja directa o cruce de ciclo de 5 amarillas
        long totalYellowsAfter = totalYellowsBefore - previousYellowsInMatch + dto.yellowCards;
        boolean crossedFiveYellowCycle = (totalYellowsAfter > totalYellowsBefore)
                && ((totalYellowsAfter / 5) > (totalYellowsBefore / 5));

        if (Boolean.TRUE.equals(dto.redCard) || crossedFiveYellowCycle) {
            Player player = appearance.getPlayer();
            player.setPlayerStatus(PlayerStatus.SUSPENDED);
            playerRepository.save(player);
        }

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