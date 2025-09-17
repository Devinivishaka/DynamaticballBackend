package com.protonestiot.dynamaticball.Service;

import com.protonestiot.dynamaticball.Entity.Role;
import com.protonestiot.dynamaticball.Entity.User;
import com.protonestiot.dynamaticball.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Add referee (only SUPER_ADMIN can call this)
    public User addReferee(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.REFEREE);
        return userRepository.save(user);
    }

    // Get all referees
    public List<User> getAllReferees() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.REFEREE)
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
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        return userRepository.save(user);
    }

    // Delete referee
    public void deleteReferee(Long id) {
        userRepository.deleteById(id);
    }
}
