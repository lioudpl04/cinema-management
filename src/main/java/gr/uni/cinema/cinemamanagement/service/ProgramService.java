package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.entity.Program;
import gr.uni.cinema.cinemamanagement.entity.ProgramState;
import gr.uni.cinema.cinemamanagement.entity.User;

import java.util.List;

public interface ProgramService {

    Program createProgram(String name, String description, User creator);

    Program updateProgram(Long programId, String name, String description, User user);

    Program changeState(Long programId, ProgramState newState, User user);

    List<Program> getProgramsByState(ProgramState state);

    Program getProgramById(Long id);

    // ✅ ΝΕΟ
    List<Program> getAllPrograms();
}

