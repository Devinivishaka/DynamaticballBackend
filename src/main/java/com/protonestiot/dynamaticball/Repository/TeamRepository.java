package com.protonestiot.dynamaticball.Repository;

import com.protonestiot.dynamaticball.Entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {


}
