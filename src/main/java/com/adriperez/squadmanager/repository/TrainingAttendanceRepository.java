package com.adriperez.squadmanager.repository;

import com.adriperez.squadmanager.dto.PlayerTrainingStatsDTO;
import com.adriperez.squadmanager.model.TrainingAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainingAttendanceRepository extends JpaRepository<TrainingAttendance, Long> {

    boolean existsByTrainingSessionIdAndPlayerId(Long trainingSessionId, Long playerId);

    Optional<TrainingAttendance> findByTrainingSessionIdAndPlayerId(Long trainingSessionId, Long playerId);

    @Query("SELECT new com.adriperez.squadmanager.dto.PlayerTrainingStatsDTO(" +
            "p.id, " +
            "COUNT(ta.id), " +
            "COUNT(CASE WHEN ta.attendanceStatus = com.adriperez.squadmanager.model.enums.AttendanceStatus.PRESENT THEN 1 END), " +
            "CAST(COALESCE(AVG(ta.rpe), 0.0) AS Double)) " +
            "FROM Player p LEFT JOIN p.trainingAttendances ta " +
            "WHERE p.id = :playerId " +
            "GROUP BY p.id")
    Optional<PlayerTrainingStatsDTO> getTrainingStatsByPlayerId(@Param("playerId") Long playerId);

    @Query("SELECT new com.adriperez.squadmanager.dto.PlayerTrainingStatsDTO(" +
            "p.id, " +
            "COUNT(ta.id), " +
            "COUNT(CASE WHEN ta.attendanceStatus = com.adriperez.squadmanager.model.enums.AttendanceStatus.PRESENT THEN 1 END), " +
            "CAST(COALESCE(AVG(ta.rpe), 0.0) AS Double)) " +
            "FROM Player p LEFT JOIN p.trainingAttendances ta " +
            "GROUP BY p.id")
    List<PlayerTrainingStatsDTO> getAllPlayersTrainingStats();
}