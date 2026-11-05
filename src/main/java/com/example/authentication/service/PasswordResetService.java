package com.example.authentication.service;

import com.example.authentication.entity.User;
import com.example.authentication.entity.PasswordResetToken;

import com.example.authentication.repository.UserRepository;
import com.example.authentication.repository.PasswordResetTokenRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // STEP 1: Request password reset

    @Transactional
    public void requestPasswordReset(String email) {

        User user = userRepository.findByEmail(email)
            .orElse(null);

        // Do not reveal whether the email exists
        if (user == null) {
            return;
        }

        // Invalidate previous reset requests
        tokenRepository.deleteByUser_Id(user.getId());

        // Generate a secure random token
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String rawToken = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes);

        // Store only the hash
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken =
            new PasswordResetToken();

        resetToken.setUser(user);
        resetToken.setTokenHash(tokenHash);

        resetToken.setExpiresAt(
            LocalDateTime.now().plusMinutes(15)
        );

        resetToken.setUsedAt(null);

        tokenRepository.save(resetToken);

        // Send original token through email
        emailService.sendPasswordResetEmail(
            user.getEmail(),
            rawToken
        );
    }


    // STEP 2: Reset password

    @Transactional
    public void resetPassword(
            String rawToken,
            String newPassword) {

        // Hash the token received from the user
        String tokenHash = hashToken(rawToken);

        PasswordResetToken resetToken = tokenRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid or expired reset token"
            ));

        // Check whether token was already used
        if (resetToken.getUsedAt() != null) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "This reset token has already been used"
            );
        }

        // Check expiration
        if (resetToken.getExpiresAt().isBefore(
                LocalDateTime.now())) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Reset token has expired"
            );
        }

        // Get associated user
        User user = resetToken.getUser();

        // Hash the new password using BCrypt
        user.setPassword(
            passwordEncoder.encode(newPassword)
        );

        // Update user
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsedAt(LocalDateTime.now());

        tokenRepository.save(resetToken);
    }


    // STEP 3: SHA-256 token hashing

    private String hashToken(String token) {

        try {

            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                token.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                "Unable to hash reset token", e
            );
        }
    }
}