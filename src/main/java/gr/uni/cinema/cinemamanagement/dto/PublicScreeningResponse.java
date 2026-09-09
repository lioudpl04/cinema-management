package gr.uni.cinema.cinemamanagement.dto;

import java.time.LocalDateTime;

public record PublicScreeningResponse(
        Long id,
        String movieTitle,
        String cast,
        String genres,
        Integer durationMinutes,
        Long programId,
        String programName,
        String auditorium,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}