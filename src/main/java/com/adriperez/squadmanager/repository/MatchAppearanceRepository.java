package com.adriperez.squadmanager.repository;

import com.adriperez.squadmanager.model.MatchAppearance;
import com.adriperez.squadmanager.model.enums.CallUpStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchAppearanceRepository extends JpaRepository<MatchAppearance, Long> {
    boolean existsByMatchIdAndPlayerId(Long matchId, Long playerId);
    Optional<MatchAppearance> findByMatchIdAndPlayerId(Long matchId, Long playerId);
    long countByMatchIdAndCallUpStatus(Long matchId, CallUpStatus callUpStatus);

}
