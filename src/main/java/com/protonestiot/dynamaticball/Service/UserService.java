package com.protonestiot.dynamaticball.Service;

import com.protonestiot.dynamaticball.Dto.RefereeResponseDto;
import com.protonestiot.dynamaticball.Entity.Role;
import com.protonestiot.dynamaticball.Entity.User;
import com.protonestiot.dynamaticball.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Add referee (only SUPER_ADMIN can call this)
    public User addReferee(User user) {
        // Store plain password directly (no encoding)
        user.setRole(Role.REFEREE);
        return userRepository.save(user);
    }

    // Get all referees (raw User objects - not used in controller anymore)
    public List<User> getAllReferees() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.REFEREE)
                .toList();
    }

    // Get all referees as DTOs (Super Admin view with password)
    public List<RefereeResponseDto> getAllRefereesDto() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.REFEREE)
                .map(user -> new RefereeResponseDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getUsername(),
                        user.getPassword(),
                        "EDIT/REMOVE"
                ))
                .toList();
    }

    // Update referee
    public User updateReferee(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Referee not found"));

        if (updatedUser.getFirstName() != null) user.setFirstName(updatedUser.getFirstName());
        if (updatedUser.getLastName() != null) user.setLastName(updatedUser.getLastName());
        if (updatedUser.getUsername() != null) user.setUsername(updatedUser.getUsername());
        if (updatedUser.getPassword() != null) {
            // Store plain password (no encoding)
            user.setPassword(updatedUser.getPassword());
        }
        return userRepository.save(user);
    }

    // Delete referee
    public void deleteReferee(Long id) {
        userRepository.deleteById(id);
    }
}
