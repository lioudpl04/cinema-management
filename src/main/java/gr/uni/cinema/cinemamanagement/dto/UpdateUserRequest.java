package gr.uni.cinema.cinemamanagement.dto;

public record UpdateUserRequest(
        String username,
        String fullName
) {
}