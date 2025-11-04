package com.protonestiot.dynamaticball.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
            @UniqueConstraint(columnNames = "username") // username must be unique
    }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //  Add formatted user ID field
    @Column(unique = true)
    private String userId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Username is required")
    private String username;

    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;


    public void setUsername(String username) {
        if (username != null) {
            this.username = username.toLowerCase();
        }
    }


    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;


    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (lastLogin == null) lastLogin = LocalDateTime.now();
    }

    @PostPersist
    protected void onPostPersist() {
        if (this.userId == null) {
            this.userId = String.format("U_%03d", this.id);
        }
    }



    private String generateUserId() {
        return String.format("U_%03d", (int) (System.currentTimeMillis() % 1000));
    }


    @PreUpdate
    protected void onUpdate() {
        lastLogin = LocalDateTime.now(); // update lastLogin every time entity is updated
    }

}
