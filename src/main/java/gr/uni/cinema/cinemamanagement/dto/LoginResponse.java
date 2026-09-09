package gr.uni.cinema.cinemamanagement.dto;

import gr.uni.cinema.cinemamanagement.entity.UserRole;

public record LoginResponse(
        Long userId,
        String username,
        UserRole role,
        String token
) {
}