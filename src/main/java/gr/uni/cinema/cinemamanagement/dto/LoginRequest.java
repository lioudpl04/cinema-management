package gr.uni.cinema.cinemamanagement.dto;

public record LoginRequest(
        String username,
        String password
) {
}