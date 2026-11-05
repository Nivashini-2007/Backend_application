package com.example.authentication.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(
            String recipientEmail,
            String token) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(senderEmail);
        message.setTo(recipientEmail);

        message.setSubject("Password Reset Request");

        message.setText(
            "Hello,\n\n" +
            "We received a request to reset your password.\n\n" +
            "Your password reset token is:\n\n" +
            token + "\n\n" +
            "This token expires in 15 minutes and can only be used once.\n\n" +
            "If you did not request this, please ignore this email."
        );

        mailSender.send(message);
    }
}