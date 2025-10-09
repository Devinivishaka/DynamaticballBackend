package com.protonestiot.dynamaticball.Controller;

import com.protonestiot.dynamaticball.Dto.RefereeResponseDto;
import com.protonestiot.dynamaticball.Entity.User;
import com.protonestiot.dynamaticball.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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


    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/referees")
    public ResponseEntity<User> addReferee(@RequestBody @Valid User user) {
        return ResponseEntity.ok(userService.addReferee(user));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/referees")
    public ResponseEntity<List<RefereeResponseDto>> getAllReferees() {
        return ResponseEntity.ok(userService.getAllRefereesDto());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/referees/{id}")
    public ResponseEntity<User> updateReferee(@PathVariable Long id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateReferee(id, user));
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/referees/{id}")
    public ResponseEntity<String> deleteReferee(@PathVariable Long id) {
        userService.deleteReferee(id);
        return ResponseEntity.ok("Referee deleted successfully");
    }
}
