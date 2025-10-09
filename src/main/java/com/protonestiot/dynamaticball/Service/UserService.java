package com.protonestiot.dynamaticball.Service;

import com.protonestiot.dynamaticball.Dto.RefereeResponseDto;
import com.protonestiot.dynamaticball.Entity.Role;
import com.protonestiot.dynamaticball.Entity.User;
import com.protonestiot.dynamaticball.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Add referee (only SUPER_ADMIN can call this)
    public User addReferee(User user) {

        user.setRole(Role.REFEREE);

        //  Generate formatted ID based on user count
        long count = userRepository.count() + 1;
        user.setUserId(String.format("U_%03d", count));

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

    // ✅ Fetch paginated + searchable user list
    public Map<String, Object> getUsers(int page, int limit, String search) {

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<User> usersPage;

        if (search != null && !search.trim().isEmpty()) {
            usersPage = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                    search, search, search, pageable
            );
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        //  Format response
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("users", usersPage.getContent().stream().map(this::convertToUserResponse).toList());

        Map<String, Object> pagination = new LinkedHashMap<>();
        pagination.put("currentPage", usersPage.getNumber() + 1);
        pagination.put("totalPages", usersPage.getTotalPages());
        pagination.put("totalItems", usersPage.getTotalElements());
        pagination.put("itemsPerPage", limit);

        data.put("pagination", pagination);
        response.put("data", data);

        return response;
    }

    //  Convert User entity to response format
    private Map<String, Object> convertToUserResponse(User user) {
        Map<String, Object> userMap = new LinkedHashMap<>();

        userMap.put("userId", user.getUserId());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("username", user.getUsername());
        userMap.put("createdAt", user.getCreatedAt());
        userMap.put("lastLogin", user.getLastLogin());
        return userMap;
    }

}
