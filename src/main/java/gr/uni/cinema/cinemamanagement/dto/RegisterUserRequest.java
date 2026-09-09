package gr.uni.cinema.cinemamanagement.dto;

public record RegisterUserRequest(
        String username,
        String fullName,
        String password
) {
}