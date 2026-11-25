package com.adriperez.squadmanager.repository;

import com.adriperez.squadmanager.model.MatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchEventRepository extends JpaRepository<MatchEvent, Long> {

    @Query("SELECT e FROM MatchEvent e JOIN FETCH e.player WHERE e.match.id = :matchId ORDER BY e.minute ASC, e.id ASC")
    List<MatchEvent> findByMatchIdWithPlayerOrderByMinuteAsc(@Param("matchId") Long matchId);

    Optional<MatchEvent> findByIdAndMatchId(Long id, Long matchId);
}