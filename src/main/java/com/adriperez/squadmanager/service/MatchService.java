package com.adriperez.squadmanager.service;

import com.adriperez.squadmanager.dto.*;
import com.adriperez.squadmanager.exception.BusinessRuleException;
import com.adriperez.squadmanager.exception.ResourceNotFoundException;
import com.adriperez.squadmanager.model.Match;
import com.adriperez.squadmanager.model.MatchAppearance;
import com.adriperez.squadmanager.model.Player;
import com.adriperez.squadmanager.model.enums.CallUpStatus;
import com.adriperez.squadmanager.model.enums.PlayerStatus;
import com.adriperez.squadmanager.repository.MatchAppearanceRepository;
import com.adriperez.squadmanager.repository.MatchRepository;
import com.adriperez.squadmanager.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class MatchService {
    private final MatchRepository matchRepository;
    private final MatchAppearanceRepository matchAppearanceRepository;
    private final PlayerRepository playerRepository;

    public MatchService(MatchRepository matchRepository, MatchAppearanceRepository matchAppearanceRepository, PlayerRepository playerRepository) {
        this.matchRepository = matchRepository;
        this.matchAppearanceRepository = matchAppearanceRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public MatchSummaryResponseDTO createMatch(MatchRequestDTO dto) {
        Match match = new Match(
                dto.opponent,
                dto.matchDate,
                dto.competition,
                dto.homeMatch);
        Match savedMatch = matchRepository.save(match);

        return MatchSummaryResponseDTO.fromEntity(savedMatch);
    }

    public List<MatchSummaryResponseDTO> getAllMatches() {
        return matchRepository.findAllByOrderByMatchDateAsc()
                .stream()
                .map(MatchSummaryResponseDTO::fromEntity) // Llama al constructor del DTO
                .toList();
    }

    public MatchDetailResponseDTO getMatchById(Long id) {
        var match = matchRepository.findByIdWithAppearances(id);
        if (match.isEmpty()) {
            throw new ResourceNotFoundException("Partido no encontrado con id: " + id);
        }
        return MatchDetailResponseDTO.fromEntity(match.get());
    }

    @Transactional
    public MatchSummaryResponseDTO updateMatchResult(Long id, MatchResultRequestDTO dto) {
        var match = matchRepository.findById(id);
        if (match.isEmpty()) {
            throw new ResourceNotFoundException("El partido está vacío");
        }
        Match currentMatch = match.get();
        currentMatch.setGoalsFor(dto.goalsFor);
        currentMatch.setGoalsAgainst(dto.goalsAgainst);
        return MatchSummaryResponseDTO.fromEntity(currentMatch);
    }

    @Transactional
    public MatchAppearanceResponseDTO addPlayerToMatch(Long matchId, CallUpRequestDTO dto) {
        // 1. Buscar Match por matchId
        var matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            throw new ResourceNotFoundException("Partido no encontrado con id: " + matchId);
        }
        Match match = matchOpt.get();

        // 2. Buscar Player por dto.playerId()
        var playerOpt = playerRepository.findById(dto.playerId);
        if (playerOpt.isEmpty()) {
            throw new ResourceNotFoundException("Jugador no encontrado con id: " + dto.playerId);
        }
        Player player = playerOpt.get();

        // 3. Verificar si el jugador ya está registrado en este partido
        if (matchAppearanceRepository.existsByMatchIdAndPlayerId(matchId, dto.playerId)) {
            throw new BusinessRuleException("El jugador ya está registrado en este partido");
        }

        // 4. Validaciones según el estado de la convocatoria
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

        // 5. Crear MatchAppearance e inicializar estadísticas
        MatchAppearance appearance = new MatchAppearance();
        appearance.setMatch(match);
        appearance.setPlayer(player);
        appearance.setCallUpStatus(dto.callUpStatus);
        appearance.setMinutesPlayed(0);
        appearance.setGoals(0);
        appearance.setAssists(0);
        appearance.setYellowCards(0);
        appearance.setRedCard(false);

        // 6. Guardar y devolver DTO
        MatchAppearance savedAppearance = matchAppearanceRepository.save(appearance);
        return MatchAppearanceResponseDTO.fromEntity(savedAppearance);
    }

    @Transactional
    public MatchAppearanceResponseDTO updatePlayerAppearanceStats(Long matchId, Long playerId, MatchAppearanceStatsRequestDTO dto) {
        // 1. Buscar MatchAppearance por matchId y playerId
        var appearanceOpt = matchAppearanceRepository.findByMatchIdAndPlayerId(matchId, playerId);
        if (appearanceOpt.isEmpty()) {
            throw new ResourceNotFoundException("No se encontró participación para el jugador " + playerId + " en el partido " + matchId);
        }
        MatchAppearance appearance = appearanceOpt.get();

        // 2. Validar coherencia de convocatoria y minutos
        if (dto.callUpStatus == CallUpStatus.NOT_CALLED && dto.minutesPlayed > 0) {
            throw new BusinessRuleException("Un jugador no convocado no puede tener minutos jugados");
        }

        if (dto.minutesPlayed == 0 && (dto.goals > 0 || dto.assists > 0 || dto.yellowCards > 0 || dto.redCard)) {
            throw new BusinessRuleException("Un jugador con 0 minutos no puede registrar goles, asistencias ni tarjetas");
        }

        // 3. Validar límite de 11 titulares si pasa a ser STARTER
        if (dto.callUpStatus == CallUpStatus.STARTER && appearance.getCallUpStatus() != CallUpStatus.STARTER) {
            long currentStarters = matchAppearanceRepository.countByMatchIdAndCallUpStatus(matchId, CallUpStatus.STARTER);
            if (currentStarters >= 11) {
                throw new BusinessRuleException("Ya hay 11 titulares en la convocatoria");
            }
        }

        // 4. Actualizar todos los campos, guardar y devolver DTO
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
        var matchAppearance = matchAppearanceRepository.findByMatchIdAndPlayerId(matchId, playerId);
        if (matchAppearance.isEmpty()) {
            throw new ResourceNotFoundException("El jugador " + playerId + " no está registrado en el partido " + matchId);
        }
        matchAppearanceRepository.delete(matchAppearance.get());
    }

}
