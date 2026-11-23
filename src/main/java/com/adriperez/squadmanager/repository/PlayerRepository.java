package com.adriperez.squadmanager.repository;

import com.adriperez.squadmanager.model.Player;
import com.adriperez.squadmanager.model.enums.PlayerStatus;
import com.adriperez.squadmanager.model.enums.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    boolean existsByShirtNumber(Integer shirtNumber);
    boolean existsByShirtNumberAndIdNot(Integer shirtNumber, Long id);
    List<Player> findByPosition(Position position);
    List<Player> findByPlayerStatus(PlayerStatus playerStatus);
    List<Player> findByPositionAndPlayerStatus(Position position, PlayerStatus playerStatus);
}
