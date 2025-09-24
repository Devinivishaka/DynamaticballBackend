package com.protonestiot.dynamaticball.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String email, String otp) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(email);
        helper.setSubject("Your Password Reset OTP");
        helper.setText("<html><body>"
                + "<p>Your OTP for password reset is:</p>"
                + "<h2>" + otp + "</h2>"
                + "<p>This OTP will expire in 15 minutes.</p>"
                + "</body></html>", true);

        mailSender.send(message);
    }
}
