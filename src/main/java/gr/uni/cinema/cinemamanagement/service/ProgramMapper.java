package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.dto.ProgramResponse;
import gr.uni.cinema.cinemamanagement.entity.Program;

public class ProgramMapper {

    public static ProgramResponse toResponse(Program program) {
        return new ProgramResponse(
                program.getId(),
                program.getName(),
                program.getDescription(),
                program.getState(),
                program.getStartDate(),
                program.getEndDate(),
                program.getCreatedAt(),
                program.getCreator() != null ? program.getCreator().getUsername() : null
        );
    }
}
