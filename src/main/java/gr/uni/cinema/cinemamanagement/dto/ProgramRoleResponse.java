package gr.uni.cinema.cinemamanagement.dto;

import gr.uni.cinema.cinemamanagement.entity.ProgramRoleType;

public record ProgramRoleResponse(
        Long id,
        Long userId,
        String username,
        Long programId,
        String programName,
        ProgramRoleType role
) {
}