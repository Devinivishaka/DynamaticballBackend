package com.protonestiot.dynamaticball.Repository;

import com.protonestiot.dynamaticball.Entity.User;
import com.protonestiot.dynamaticball.Entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    VerificationToken findByUser(User user);

}
