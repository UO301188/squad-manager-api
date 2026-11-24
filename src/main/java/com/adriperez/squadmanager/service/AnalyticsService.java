package com.adriperez.squadmanager.service;

import com.adriperez.squadmanager.dto.PlayerMatchStatsDTO;
import com.adriperez.squadmanager.dto.PlayerStatsResponseDTO;
import com.adriperez.squadmanager.dto.PlayerTrainingStatsDTO;
import com.adriperez.squadmanager.exception.ResourceNotFoundException;
import com.adriperez.squadmanager.model.Player;
import com.adriperez.squadmanager.repository.MatchAppearanceRepository;
import com.adriperez.squadmanager.repository.PlayerRepository;
import com.adriperez.squadmanager.repository.TrainingAttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final PlayerRepository playerRepository;
    private final MatchAppearanceRepository matchAppearanceRepository;
    private final TrainingAttendanceRepository trainingAttendanceRepository;

    public AnalyticsService(
            PlayerRepository playerRepository,
            MatchAppearanceRepository matchAppearanceRepository,
            TrainingAttendanceRepository trainingAttendanceRepository) {
        this.playerRepository = playerRepository;
        this.matchAppearanceRepository = matchAppearanceRepository;
        this.trainingAttendanceRepository = trainingAttendanceRepository;
    }

    public PlayerStatsResponseDTO getPlayerStats(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + playerId));

        PlayerMatchStatsDTO matchStats = matchAppearanceRepository.getMatchStatsByPlayerId(playerId)
                .orElse(new PlayerMatchStatsDTO(playerId, 0L, 0L, 0L, 0L, 0L, 0L, 0L));

        PlayerTrainingStatsDTO trainingStats = trainingAttendanceRepository.getTrainingStatsByPlayerId(playerId)
                .orElse(new PlayerTrainingStatsDTO(playerId, 0L, 0L, 0.0));

        return buildStatsResponse(player, matchStats, trainingStats);
    }

    public List<PlayerStatsResponseDTO> getAllPlayersStats() {
        List<Player> players = playerRepository.findAll();

        Map<Long, PlayerMatchStatsDTO> matchStatsByPlayer = matchAppearanceRepository.getAllPlayersMatchStats()
                .stream()
                .collect(Collectors.toMap(PlayerMatchStatsDTO::playerId, Function.identity()));

        Map<Long, PlayerTrainingStatsDTO> trainingStatsByPlayer = trainingAttendanceRepository.getAllPlayersTrainingStats()
                .stream()
                .collect(Collectors.toMap(PlayerTrainingStatsDTO::playerId, Function.identity()));

        return players.stream()
                .map(player -> {
                    Long id = player.getId();
                    PlayerMatchStatsDTO matchStats = matchStatsByPlayer.getOrDefault(
                            id, new PlayerMatchStatsDTO(id, 0L, 0L, 0L, 0L, 0L, 0L, 0L));
                    PlayerTrainingStatsDTO trainingStats = trainingStatsByPlayer.getOrDefault(
                            id, new PlayerTrainingStatsDTO(id, 0L, 0L, 0.0));
                    return buildStatsResponse(player, matchStats, trainingStats);
                })
                .sorted(Comparator.comparingLong(PlayerStatsResponseDTO::totalMinutes).reversed())
                .toList();
    }

    private PlayerStatsResponseDTO buildStatsResponse(
            Player player,
            PlayerMatchStatsDTO matchStats,
            PlayerTrainingStatsDTO trainingStats) {

        long totalMinutes = matchStats.totalMinutes();
        long totalGoals = matchStats.totalGoals();
        long totalAssists = matchStats.totalAssists();
        long goalContributions = totalGoals + totalAssists;

        double goalsPer90 = totalMinutes > 0
                ? roundTwoDecimals((totalGoals * 90.0) / totalMinutes)
                : 0.0;

        long totalSessions = trainingStats.totalSessions();
        long sessionsPresent = trainingStats.sessionsPresent();

        double attendancePercentage = totalSessions > 0
                ? roundTwoDecimals((sessionsPresent * 100.0) / totalSessions)
                : 0.0;

        double averageRpe = trainingStats.averageRpe() != null
                ? roundTwoDecimals(trainingStats.averageRpe())
                : 0.0;

        return new PlayerStatsResponseDTO(
                player.getId(),
                player.getFirstName() + " " + player.getLastName(),
                player.getShirtNumber(),
                player.getPosition(),
                player.getPlayerStatus(),
                matchStats.matchesCalledUp(),
                matchStats.matchesPlayed(),
                totalMinutes,
                totalGoals,
                totalAssists,
                goalContributions,
                goalsPer90,
                matchStats.yellowCards(),
                matchStats.redCards(),
                totalSessions,
                sessionsPresent,
                attendancePercentage,
                averageRpe
        );
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}