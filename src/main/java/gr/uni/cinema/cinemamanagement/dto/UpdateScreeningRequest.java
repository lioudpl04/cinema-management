package gr.uni.cinema.cinemamanagement.dto;

import java.time.LocalDateTime;

public record UpdateScreeningRequest(
        String movieTitle,
        String cast,
        String genres,
        Integer durationMinutes,
        String auditorium,
        LocalDateTime startTime
) {
}