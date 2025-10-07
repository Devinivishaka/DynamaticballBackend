package com.protonestiot.dynamaticball.Repository;

import com.protonestiot.dynamaticball.Entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}


