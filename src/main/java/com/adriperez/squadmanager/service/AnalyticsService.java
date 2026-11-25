package com.adriperez.squadmanager.service;

import com.adriperez.squadmanager.dto.PlayerMatchStatsDTO;
import com.adriperez.squadmanager.dto.PlayerStatsResponseDTO;
import com.adriperez.squadmanager.dto.PlayerTrainingStatsDTO;
import com.adriperez.squadmanager.exception.ResourceNotFoundException;
import com.adriperez.squadmanager.model.Player;
import com.adriperez.squadmanager.repository.MatchAppearanceRepository;
import com.adriperez.squadmanager.repository.PlayerRepository;
import com.adriperez.squadmanager.repository.TrainingAttendanceRepository;
import com.adriperez.squadmanager.security.CurrentUserService;
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
    private final CurrentUserService currentUserService;

    public AnalyticsService(
            PlayerRepository playerRepository,
            MatchAppearanceRepository matchAppearanceRepository,
            TrainingAttendanceRepository trainingAttendanceRepository,
            CurrentUserService currentUserService) {
        this.playerRepository = playerRepository;
        this.matchAppearanceRepository = matchAppearanceRepository;
        this.trainingAttendanceRepository = trainingAttendanceRepository;
        this.currentUserService = currentUserService;
    }

    public PlayerStatsResponseDTO getPlayerStats(Long playerId) {
        Long userId = currentUserService.getCurrentUser().getId();

        Player player = playerRepository.findByIdAndUserId(playerId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + playerId));

        PlayerMatchStatsDTO matchStats = matchAppearanceRepository.getMatchStatsByPlayerId(playerId)
                .orElse(emptyMatchStats(playerId));

        PlayerTrainingStatsDTO trainingStats = trainingAttendanceRepository.getTrainingStatsByPlayerId(playerId)
                .orElse(new PlayerTrainingStatsDTO(playerId, 0L, 0L, 0.0));

        return buildStatsResponse(player, matchStats, trainingStats);
    }

    public List<PlayerStatsResponseDTO> getAllPlayersStats() {
        Long userId = currentUserService.getCurrentUser().getId();

        List<Player> players = playerRepository.findByUserId(userId);

        Map<Long, PlayerMatchStatsDTO> matchStatsByPlayer = matchAppearanceRepository.getAllPlayersMatchStatsByUserId(userId)
                .stream()
                .collect(Collectors.toMap(PlayerMatchStatsDTO::playerId, Function.identity()));

        Map<Long, PlayerTrainingStatsDTO> trainingStatsByPlayer = trainingAttendanceRepository.getAllPlayersTrainingStatsByUserId(userId)
                .stream()
                .collect(Collectors.toMap(PlayerTrainingStatsDTO::playerId, Function.identity()));

        return players.stream()
                .map(player -> {
                    Long id = player.getId();
                    PlayerMatchStatsDTO matchStats = matchStatsByPlayer.getOrDefault(id, emptyMatchStats(id));
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

        long matchesPlayed = matchStats.matchesPlayed();
        long totalMinutes = matchStats.totalMinutes();
        long totalGoals = matchStats.totalGoals();
        long totalAssists = matchStats.totalAssists();
        long goalContributions = totalGoals + totalAssists;

        double avgMinutesPerMatch = matchesPlayed > 0
                ? roundTwoDecimals((double) totalMinutes / matchesPlayed)
                : 0.0;

        double goalsPer90 = totalMinutes > 0
                ? roundTwoDecimals((totalGoals * 90.0) / totalMinutes)
                : 0.0;

        double assistsPer90 = totalMinutes > 0
                ? roundTwoDecimals((totalAssists * 90.0) / totalMinutes)
                : 0.0;

        double goalContributionsPer90 = totalMinutes > 0
                ? roundTwoDecimals((goalContributions * 90.0) / totalMinutes)
                : 0.0;

        long matchesFinishedPlayed = matchStats.matchesFinishedPlayed();
        double winRatePercentage = matchesFinishedPlayed > 0
                ? roundTwoDecimals((matchStats.matchesWon() * 100.0) / matchesFinishedPlayed)
                : 0.0;

        long yellowCards = matchStats.yellowCards();
        boolean warnedByCards = yellowCards > 0 && (yellowCards % 5 == 4);

        double averageRating = matchStats.averageRating() != null
                ? roundTwoDecimals(matchStats.averageRating())
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
                matchesPlayed,
                matchStats.starts(),
                matchStats.subAppearances(),
                totalMinutes,
                avgMinutesPerMatch,
                totalGoals,
                totalAssists,
                goalContributions,
                goalsPer90,
                assistsPer90,
                goalContributionsPer90,
                matchStats.cleanSheets(),
                winRatePercentage,
                yellowCards,
                matchStats.redCards(),
                warnedByCards,
                averageRating,
                totalSessions,
                sessionsPresent,
                attendancePercentage,
                averageRpe
        );
    }

    private PlayerMatchStatsDTO emptyMatchStats(Long playerId) {
        return new PlayerMatchStatsDTO(playerId, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0.0);
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}