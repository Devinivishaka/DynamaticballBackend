package com.protonestiot.dynamaticball.Entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Date;

@Getter
@Entity
public class VerificationToken {

    // Getters and Setters
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private User user;

    private String otpHash;  // store hashed OTP

    @Temporal(TemporalType.TIMESTAMP)
    private Date expiryDate;

    public void setId(Long id) { this.id = id; }

    public void setUser(User user) { this.user = user; }

    public void setOtpHash(String otpHash) { this.otpHash = otpHash; }

    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
}
