package com.example.authentication.controller;

import com.example.authentication.dto.ForgotPasswordRequest;
import com.example.authentication.dto.ResetPasswordRequest;
import com.example.authentication.dto.MessageResponse;

import com.example.authentication.service.PasswordResetService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(
            PasswordResetService passwordResetService) {

        this.passwordResetService = passwordResetService;
    }

    // Forgot password API

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordResetService.requestPasswordReset(
            request.email()
        );

        return ResponseEntity.ok(
            new MessageResponse(
                "If an account exists for this email, password reset instructions will be sent."
            )
        );
    }


    // Reset password API

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordResetService.resetPassword(
            request.token(),
            request.newPassword()
        );

        return ResponseEntity.ok(
            new MessageResponse(
                "Password reset successfully. You can now log in."
            )
        );
    }
}