package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.RefereeResponseDto;
import com.protonestiot.dynamaticball.Dto.UserDto;
import com.protonestiot.dynamaticball.Entity.User;
import com.protonestiot.dynamaticball.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    // ---------------- USER CRUD ----------------
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search)
    {
        return ResponseEntity.ok(userService.getUsers(page, limit, search));
    }

    // ---------------- ADD REFEREE ----------------
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/referees")
    public ResponseEntity<User> addReferee(@RequestBody @Valid UserDto userDto) {
        return ResponseEntity.ok(userService.addReferee(userDto));
    }

    // ---------------- GET ALL REFEREES ----------------
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/referees")
    public ResponseEntity<List<RefereeResponseDto>> getAllReferees() {
        return ResponseEntity.ok(userService.getAllRefereesDto());
    }

    // ---------------- GET USER BY ID ----------------
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }


    // ---------------- UPDATE REFEREE ----------------
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/referees/{id}")
    public ResponseEntity<User> updateReferee(@PathVariable Long id, @RequestBody @Valid UserDto userDto) {
        return ResponseEntity.ok(userService.updateReferee(id, userDto));
    }

    // ---------------- DELETE REFEREE ----------------
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/referees/{id}")
    public ResponseEntity<String> deleteReferee(@PathVariable Long id) {
        userService.deleteReferee(id);
        return ResponseEntity.ok("Referee deleted successfully");
    }
}
