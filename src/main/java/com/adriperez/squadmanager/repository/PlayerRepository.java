package com.adriperez.squadmanager.repository;

import com.adriperez.squadmanager.model.Player;
import com.adriperez.squadmanager.model.enums.PlayerStatus;
import com.adriperez.squadmanager.model.enums.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    boolean existsByUserIdAndShirtNumber(Long userId, Integer shirtNumber);
    boolean existsByUserIdAndShirtNumberAndIdNot(Long userId, Integer shirtNumber, Long id);
    Optional<Player> findByIdAndUserId(Long id, Long userId);
    List<Player> findByUserId(Long userId);
    List<Player> findByUserIdAndPosition(Long userId, Position position);
    List<Player> findByUserIdAndPlayerStatus(Long userId, PlayerStatus playerStatus);
    List<Player> findByUserIdAndPositionAndPlayerStatus(Long userId, Position position, PlayerStatus playerStatus);
}