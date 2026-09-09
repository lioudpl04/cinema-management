package gr.uni.cinema.cinemamanagement.dto;

public record ReviewScreeningRequest(
        Integer score,
        String comments
) {
}