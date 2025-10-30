package com.protonestiot.dynamaticball.Repository;

import com.protonestiot.dynamaticball.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
   Optional<User> findByUsernameIgnoreCase(String username);
   boolean existsByUsername(String username);


    // ✅ Custom search by name or username
    Page<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String firstName,
            String lastName,
            String username,
            Pageable pageable
    );

}
