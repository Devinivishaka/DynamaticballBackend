package com.protonestiot.dynamaticball.Service;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.communication.email.models.EmailSendStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${azure.communication.connection-string}")
    private String connectionString;

    @Value("${azure.communication.sender-email}")
    private String senderEmail;

    private EmailClient emailClient;

    @PostConstruct
    public void init() {
        if (connectionString != null && !connectionString.isBlank()) {
            this.emailClient = new EmailClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        }
    }

    @Async
    public void sendOtpEmail(String recipientEmail, String otp) throws MessagingException {
        if (emailClient == null) {
            init();
        }

        String subject = "Your Password Reset OTP";
        String htmlContent = "<html><body>"
                + "<p>Your OTP for password reset is:</p>"
                + "<h2>" + otp + "</h2>"
                + "<p>This OTP will expire in 5 minutes.</p>"
                + "</body></html>";

        try {
            EmailMessage message = new EmailMessage()
                    .setSenderAddress(senderEmail)
                    .setToRecipients(recipientEmail)
                    .setSubject(subject)
                    .setBodyHtml(htmlContent);

            SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
            PollResponse<EmailSendResult> response = poller.waitForCompletion();

            if (response.getValue().getStatus() != EmailSendStatus.SUCCEEDED) {
                throw new MessagingException("Azure Email send failed with status: " + response.getValue().getStatus());
            }
        } catch (Exception e) {
            throw new MessagingException("Failed to send OTP email via Azure Communication Services: " + e.getMessage(), e);
        }
    }
}

