package com.protonestiot.dynamaticball.Service;

import com.protonestiot.dynamaticball.Dto.UserDto;
import com.protonestiot.dynamaticball.Entity.Role;
import com.protonestiot.dynamaticball.Entity.User;
import com.protonestiot.dynamaticball.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserDto sampleUserDto;

    @BeforeEach
    void setUp() {
        sampleUserDto = new UserDto();
        sampleUserDto.setFirstName("John");
        sampleUserDto.setLastName("Doe");
        sampleUserDto.setUsername("john.doe@example.com");
        sampleUserDto.setPassword("password123");
    }

    @Test
    void addUser_withNoRole_defaultsToReferee() {
        sampleUserDto.setRole(null);
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userService.addUser(sampleUserDto);

        assertNotNull(createdUser);
        assertEquals(Role.REFEREE, createdUser.getRole());
        assertEquals("john.doe@example.com", createdUser.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addUser_withExplicitRefereeRole_succeeds() {
        sampleUserDto.setRole(Role.REFEREE);
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userService.addUser(sampleUserDto);

        assertNotNull(createdUser);
        assertEquals(Role.REFEREE, createdUser.getRole());
        assertEquals("john.doe@example.com", createdUser.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addUser_withProfileImageUrl_savesProfileImage() {
        sampleUserDto.setProfileImageUrl("avatar_blob_123.png");
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userService.addUser(sampleUserDto);

        assertNotNull(createdUser);
        assertEquals("avatar_blob_123.png", createdUser.getProfileImageUrl());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addUser_firstSuperAdmin_succeeds() {
        sampleUserDto.setRole(Role.SUPER_ADMIN);
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByRole(Role.SUPER_ADMIN)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userService.addUser(sampleUserDto);

        assertNotNull(createdUser);
        assertEquals(Role.SUPER_ADMIN, createdUser.getRole());
    }

    @Test
    void addUser_secondSuperAdmin_throwsException() {
        sampleUserDto.setRole(Role.SUPER_ADMIN);
        when(userRepository.existsByUsernameIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.existsByRole(Role.SUPER_ADMIN)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.addUser(sampleUserDto)
        );

        assertTrue(exception.getMessage().contains("Only one Super Admin is allowed"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateRefereeByUserId_toSuperAdmin_whenAnotherSuperAdminExists_throwsException() {
        String userId = "U_002";
        User existingUser = new User();
        existingUser.setUserId(userId);
        existingUser.setRole(Role.REFEREE);
        existingUser.setUsername("referee@example.com");

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByRoleAndUserIdNot(Role.SUPER_ADMIN, userId)).thenReturn(true);

        UserDto updateDto = new UserDto();
        updateDto.setRole(Role.SUPER_ADMIN);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateRefereeByUserId(userId, updateDto)
        );

        assertTrue(exception.getMessage().contains("Only one Super Admin is allowed"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUserByUserId_superAdmin_throwsException() {
        String userId = "U_001";
        User adminUser = new User();
        adminUser.setUserId(userId);
        adminUser.setRole(Role.SUPER_ADMIN);

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(adminUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteUserByUserId(userId)
        );

        assertEquals("Super Admin cannot be deleted", exception.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUserByUserId_referee_succeeds() {
        String userId = "U_002";
        User refereeUser = new User();
        refereeUser.setUserId(userId);
        refereeUser.setRole(Role.REFEREE);

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(refereeUser));

        User deleted = userService.deleteUserByUserId(userId);

        assertEquals(userId, deleted.getUserId());
        verify(userRepository).delete(refereeUser);
    }

    @Test
    void getUsers_excludesSuperAdmin_withoutSearch() {
        User referee = new User();
        referee.setUserId("U_001");
        referee.setRole(Role.REFEREE);
        referee.setUsername("referee@example.com");

        when(userRepository.findByRoleNot(eq(Role.SUPER_ADMIN), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(referee)));

        Map<String, Object> result = userService.getUsers(1, 10, null);

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        Map<?, ?> data = (Map<?, ?>) result.get("data");
        List<?> users = (List<?>) data.get("users");
        assertEquals(1, users.size());
        verify(userRepository).findByRoleNot(eq(Role.SUPER_ADMIN), any(Pageable.class));
    }

    @Test
    void getUsers_excludesSuperAdmin_withSearch() {
        User referee = new User();
        referee.setUserId("U_001");
        referee.setRole(Role.REFEREE);
        referee.setUsername("john@example.com");

        when(userRepository.searchUsersExcludingRole(eq("john"), eq(Role.SUPER_ADMIN), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(referee)));

        Map<String, Object> result = userService.getUsers(1, 10, "john");

        assertNotNull(result);
        Map<?, ?> data = (Map<?, ?>) result.get("data");
        List<?> users = (List<?>) data.get("users");
        assertEquals(1, users.size());
        verify(userRepository).searchUsersExcludingRole(eq("john"), eq(Role.SUPER_ADMIN), any(Pageable.class));
    }
}
