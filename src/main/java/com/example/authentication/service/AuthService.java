package com.example.authentication.service;

import com.example.authentication.dto.SignupRequest;
import com.example.authentication.dto.LoginRequest;
import com.example.authentication.dto.AuthResponse;

import com.example.authentication.entity.User;
import com.example.authentication.entity.Group;

import com.example.authentication.repository.UserRepository;
import com.example.authentication.repository.GroupRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            GroupRepository groupRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // SIGNUP
    @Transactional
    public AuthResponse signup(SignupRequest request) {

        // Check duplicate name
        if (userRepository.existsByName(request.username())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Name already exists"
            );
        }

        // Check duplicate email
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email already exists"
            );
        }

        // Check whether selected technology group exists
        Group preferredGroup = groupRepository
            .findByNameIgnoreCase(request.preferredTechnology())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid technology preference"
            ));

        // Create user
        User user = new User();

        user.setName(request.username());
        user.setEmail(request.email());

        // Hash password
        user.setPassword(
            passwordEncoder.encode(request.password())
        );

        // Store preference, but do not assign group yet
        user.setPreferredTechnology(preferredGroup.getName());

        user.setGroup(null);

        // Save user
        userRepository.save(user);

        return new AuthResponse(
            "Signup successful. Your group will be assigned on first login.",
            user.getName(),
            null
        );
    }


    // LOGIN
    @Transactional
    public AuthResponse login(LoginRequest request) {

        // Find user by name
        User user = userRepository
            .findByName(request.username())
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
            ));

        // Verify password
        boolean passwordMatches = passwordEncoder.matches(
            request.password(),
            user.getPassword()
        );

        if (!passwordMatches) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
            );
        }

        // Assign group only on first successful login
        if (user.getGroup() == null) {

            Group group = groupRepository
                .findByNameIgnoreCase(user.getPreferredTechnology())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Preferred technology group not found"
                ));

            user.setGroup(group);

            userRepository.save(user);

            return new AuthResponse(
                "Login successful. Group assigned successfully.",
                user.getName(),
                group.getName()
            );
        }

        // If already assigned, retain existing group
        return new AuthResponse(
            "Login successful. Existing group retained.",
            user.getName(),
            user.getGroup().getName()
        );
    }
}