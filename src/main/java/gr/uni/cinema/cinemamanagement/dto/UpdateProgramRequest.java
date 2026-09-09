package gr.uni.cinema.cinemamanagement.dto;

import java.time.LocalDate;

public record UpdateProgramRequest(
        String name,
        String description,
        LocalDate startDate,
        LocalDate endDate
) {
}