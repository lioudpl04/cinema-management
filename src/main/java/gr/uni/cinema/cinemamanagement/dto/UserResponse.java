package gr.uni.cinema.cinemamanagement.dto;

import gr.uni.cinema.cinemamanagement.entity.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        UserRole role,
        boolean active,
        int failedLoginAttempts,
        LocalDateTime createdAt
) {
}