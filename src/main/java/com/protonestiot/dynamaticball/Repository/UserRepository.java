package com.protonestiot.dynamaticball.Repository;

import com.protonestiot.dynamaticball.Entity.Role;
import com.protonestiot.dynamaticball.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByUsername(String username);

    Optional<User> findByUserId(String userId);

    Page<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(
            String firstName,
            String lastName,
            String username,
            Pageable pageable

    );

    Page<User> findByRoleNot(Role role, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.role <> :role AND (" +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsersExcludingRole(@Param("search") String search, @Param("role") Role role, Pageable pageable);

    boolean existsByRole(Role role);

    boolean existsByRoleAndUserIdNot(Role role, String userId);
}
