package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.entity.*;

import java.time.LocalDate;
import java.util.List;

public interface ProgramService {

    // Dimiourgia neou program.
    Program createProgram(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            User creator
    );

    // Enimerosi stoixeion enos program.
    Program updateProgram(
            Long programId,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            User user
    );

    // Diagrafi program.
    void deleteProgram(
            Long programId,
            User user
    );

    // Allagi tou state tou program.
    Program changeState(
            Long programId,
            ProgramState newState,
            User user
    );

    List<Program> getProgramsByState(
            ProgramState state
    );

    Program getProgramById(
            Long id
    );

    List<Program> getAllPrograms();

    // Anazitisi program me filters.
    List<Program> searchPrograms(
            String name,
            LocalDate fromDate,
            LocalDate toDate,
            User currentUser
    );

    // Prosthetei rolo se user gia sygkekrimeno program.
    ProgramRole addRole(
            Long programId,
            User targetUser,
            ProgramRoleType role,
            User requestingUser
    );

    // Epistrefei program mono an o user exei dikaioma na to dei.
    Program getVisibleProgramById(
            Long id,
            User currentUser
    );
}