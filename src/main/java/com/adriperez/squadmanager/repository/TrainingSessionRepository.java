package com.adriperez.squadmanager.repository;

import com.adriperez.squadmanager.model.TrainingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    List<TrainingSession> findAllByOrderBySessionDateDesc();

    @Query("SELECT DISTINCT ts FROM TrainingSession ts " +
            "LEFT JOIN FETCH ts.trainingAttendances ta " +
            "LEFT JOIN FETCH ta.player " +
            "WHERE ts.id = :id")
    Optional<TrainingSession> findByIdWithAttendances(@Param("id") Long id);
}