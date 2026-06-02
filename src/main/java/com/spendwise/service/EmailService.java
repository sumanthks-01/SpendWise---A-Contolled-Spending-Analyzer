package com.spendwise.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String userName, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("SpendWise - Password Reset Request");
        message.setText("Hi " + userName + ",\n\n" +
                "You requested a password reset for your SpendWise account.\n" +
                "Click the link below to reset it:\n\n" +
                resetLink + "\n\n" +
                "This link will expire in 15 minutes.\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Thanks,\nThe SpendWise Team");

        mailSender.send(message);
    }
}
