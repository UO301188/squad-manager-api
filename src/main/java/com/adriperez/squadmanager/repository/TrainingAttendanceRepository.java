package com.adriperez.squadmanager.repository;

import com.adriperez.squadmanager.model.TrainingAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainingAttendanceRepository extends JpaRepository<TrainingAttendance, Long> {

    boolean existsByTrainingSessionIdAndPlayerId(Long trainingSessionId, Long playerId);

    Optional<TrainingAttendance> findByTrainingSessionIdAndPlayerId(Long trainingSessionId, Long playerId);
}