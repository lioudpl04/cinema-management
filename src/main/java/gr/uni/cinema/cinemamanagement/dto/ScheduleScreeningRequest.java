package gr.uni.cinema.cinemamanagement.dto;

import java.time.LocalDateTime;

public record ScheduleScreeningRequest(
        String auditorium,
        LocalDateTime startTime,
        LocalDateTime endTime
) {}
