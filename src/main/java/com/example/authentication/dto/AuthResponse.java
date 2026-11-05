package com.example.authentication.dto;

public record AuthResponse(
    String message,
    String username,
    String groupName
) {
}