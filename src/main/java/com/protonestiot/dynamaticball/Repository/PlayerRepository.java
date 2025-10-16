package com.protonestiot.dynamaticball.Repository;

import com.protonestiot.dynamaticball.Entity.Player;
import com.protonestiot.dynamaticball.Entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    long countByTeam(Team team);

}
