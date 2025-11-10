package com.protonestiot.dynamaticball.Service;

import com.protonestiot.dynamaticball.Dto.RefereeResponseDto;
import com.protonestiot.dynamaticball.Dto.UserDto;
import com.protonestiot.dynamaticball.Entity.Role;
import com.protonestiot.dynamaticball.Entity.User;
import com.protonestiot.dynamaticball.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User addReferee(UserDto userDto) {
        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setUsername(userDto.getUsername());
        user.setPassword(userDto.getPassword());
        user.setRole(Role.REFEREE);
        return userRepository.save(user);
    }

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

    public User updateRefereeByUserId(String userId, UserDto userDto) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Referee not found with ID: " + userId));

        if (userDto.getFirstName() != null) user.setFirstName(userDto.getFirstName());
        if (userDto.getLastName() != null) user.setLastName(userDto.getLastName());
        if (userDto.getUsername() != null) user.setUsername(userDto.getUsername());
        if (userDto.getPassword() != null) user.setPassword(userDto.getPassword());

        return userRepository.save(user);
    }

    public void deleteRefereeByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Referee not found with ID: " + userId));
        userRepository.delete(user);
    }

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

    public Map<String, Object> getUserByUserId(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return convertToUserResponse(user);
    }

    private Map<String, Object> convertToUserResponse(User user) {
        Map<String, Object> userMap = new LinkedHashMap<>();
        userMap.put("userId", user.getUserId());
        userMap.put("firstName", user.getFirstName());
        userMap.put("lastName", user.getLastName());
        userMap.put("username", user.getUsername());
        userMap.put("password", user.getPassword());
        userMap.put("createdAt", user.getCreatedAt());
        userMap.put("lastLogin", user.getLastLogin());
        return userMap;
    }
}
