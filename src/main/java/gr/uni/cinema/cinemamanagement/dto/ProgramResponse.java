package gr.uni.cinema.cinemamanagement.dto;

import gr.uni.cinema.cinemamanagement.entity.ProgramState;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProgramResponse(
        Long id,
        String name,
        String description,
        ProgramState state,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt,
        String createdByUsername
) {
}
