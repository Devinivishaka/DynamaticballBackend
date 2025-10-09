package com.protonestiot.dynamaticball.Repository;

import com.protonestiot.dynamaticball.Entity.GameSetup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameSetupRepository extends JpaRepository<GameSetup, Long> {
    Optional<GameSetup> findBySetupCode(String setupCode);

    long countBySetupCodeIsNotNull();
}

