package com.protonestiot.dynamaticball.Repository;

import com.protonestiot.dynamaticball.Entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByPlayerCode(String playerCode);
    void deleteByPlayerCode(String playerCode);
}
