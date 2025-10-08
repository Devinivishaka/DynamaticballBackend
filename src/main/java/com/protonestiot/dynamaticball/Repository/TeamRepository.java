package com.protonestiot.dynamaticball.Repository;

import com.protonestiot.dynamaticball.Entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
