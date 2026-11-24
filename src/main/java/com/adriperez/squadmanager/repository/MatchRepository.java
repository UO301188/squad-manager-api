package com.adriperez.squadmanager.repository;

import com.adriperez.squadmanager.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByUserIdOrderByMatchDateAsc(Long userId);
    Optional<Match> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT DISTINCT m FROM Match m LEFT JOIN FETCH m.matchAppearances ma LEFT JOIN FETCH ma.player " +
            "WHERE m.id = :id AND m.user.id = :userId")
    Optional<Match> findByIdAndUserIdWithAppearances(@Param("id") Long id, @Param("userId") Long userId);
}