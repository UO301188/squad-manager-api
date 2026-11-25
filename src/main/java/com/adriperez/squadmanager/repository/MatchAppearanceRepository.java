package com.adriperez.squadmanager.repository;

import com.adriperez.squadmanager.dto.PlayerMatchStatsDTO;
import com.adriperez.squadmanager.model.MatchAppearance;
import com.adriperez.squadmanager.model.enums.CallUpStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchAppearanceRepository extends JpaRepository<MatchAppearance, Long> {

    boolean existsByMatchIdAndPlayerId(Long matchId, Long playerId);

    Optional<MatchAppearance> findByMatchIdAndPlayerId(Long matchId, Long playerId);

    long countByMatchIdAndCallUpStatus(Long matchId, CallUpStatus callUpStatus);

    @Query("SELECT new com.adriperez.squadmanager.dto.PlayerMatchStatsDTO(" +
            "p.id, " +
            "COUNT(CASE WHEN ma.callUpStatus <> com.adriperez.squadmanager.model.enums.CallUpStatus.NOT_CALLED THEN 1 END), " +
            "COUNT(CASE WHEN ma.minutesPlayed > 0 THEN 1 END), " +
            "COUNT(CASE WHEN ma.callUpStatus = com.adriperez.squadmanager.model.enums.CallUpStatus.STARTER THEN 1 END), " +
            "COUNT(CASE WHEN ma.callUpStatus = com.adriperez.squadmanager.model.enums.CallUpStatus.SUBSTITUTE AND ma.minutesPlayed > 0 THEN 1 END), " +
            "CAST(COALESCE(SUM(ma.minutesPlayed), 0) AS Long), " +
            "CAST(COALESCE(SUM(ma.goals), 0) AS Long), " +
            "CAST(COALESCE(SUM(ma.assists), 0) AS Long), " +
            "COUNT(CASE WHEN ma.minutesPlayed >= 45 AND m.goalsAgainst = 0 AND m.goalsFor IS NOT NULL THEN 1 END), " +
            "COUNT(CASE WHEN ma.minutesPlayed > 0 AND m.goalsFor IS NOT NULL AND m.goalsAgainst IS NOT NULL AND m.goalsFor > m.goalsAgainst THEN 1 END), " +
            "COUNT(CASE WHEN ma.minutesPlayed > 0 AND m.goalsFor IS NOT NULL AND m.goalsAgainst IS NOT NULL THEN 1 END), " +
            "CAST(COALESCE(SUM(ma.yellowCards), 0) AS Long), " +
            "COUNT(CASE WHEN ma.redCard = true THEN 1 END)) " +
            "FROM Player p LEFT JOIN p.matchAppearances ma LEFT JOIN ma.match m " +
            "WHERE p.id = :playerId " +
            "GROUP BY p.id")
    Optional<PlayerMatchStatsDTO> getMatchStatsByPlayerId(@Param("playerId") Long playerId);

    @Query("SELECT new com.adriperez.squadmanager.dto.PlayerMatchStatsDTO(" +
            "p.id, " +
            "COUNT(CASE WHEN ma.callUpStatus <> com.adriperez.squadmanager.model.enums.CallUpStatus.NOT_CALLED THEN 1 END), " +
            "COUNT(CASE WHEN ma.minutesPlayed > 0 THEN 1 END), " +
            "COUNT(CASE WHEN ma.callUpStatus = com.adriperez.squadmanager.model.enums.CallUpStatus.STARTER THEN 1 END), " +
            "COUNT(CASE WHEN ma.callUpStatus = com.adriperez.squadmanager.model.enums.CallUpStatus.SUBSTITUTE AND ma.minutesPlayed > 0 THEN 1 END), " +
            "CAST(COALESCE(SUM(ma.minutesPlayed), 0) AS Long), " +
            "CAST(COALESCE(SUM(ma.goals), 0) AS Long), " +
            "CAST(COALESCE(SUM(ma.assists), 0) AS Long), " +
            "COUNT(CASE WHEN ma.minutesPlayed >= 45 AND m.goalsAgainst = 0 AND m.goalsFor IS NOT NULL THEN 1 END), " +
            "COUNT(CASE WHEN ma.minutesPlayed > 0 AND m.goalsFor IS NOT NULL AND m.goalsAgainst IS NOT NULL AND m.goalsFor > m.goalsAgainst THEN 1 END), " +
            "COUNT(CASE WHEN ma.minutesPlayed > 0 AND m.goalsFor IS NOT NULL AND m.goalsAgainst IS NOT NULL THEN 1 END), " +
            "CAST(COALESCE(SUM(ma.yellowCards), 0) AS Long), " +
            "COUNT(CASE WHEN ma.redCard = true THEN 1 END)) " +
            "FROM Player p LEFT JOIN p.matchAppearances ma LEFT JOIN ma.match m " +
            "WHERE p.user.id = :userId " +
            "GROUP BY p.id")
    List<PlayerMatchStatsDTO> getAllPlayersMatchStatsByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(ma.yellowCards), 0) FROM MatchAppearance ma WHERE ma.player.id = :playerId")
    long getTotalYellowCardsByPlayerId(@Param("playerId") Long playerId);

    @Query("SELECT ma.player.id, COALESCE(SUM(ma.yellowCards), 0) FROM MatchAppearance ma WHERE ma.player.user.id = :userId GROUP BY ma.player.id")
    List<Object[]> getYellowCardsCountByUserId(@Param("userId") Long userId);
}