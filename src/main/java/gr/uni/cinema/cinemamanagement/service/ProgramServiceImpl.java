package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.entity.Program;
import gr.uni.cinema.cinemamanagement.entity.ProgramState;
import gr.uni.cinema.cinemamanagement.entity.User;
import gr.uni.cinema.cinemamanagement.entity.UserRole;
import gr.uni.cinema.cinemamanagement.exception.ForbiddenOperationException;
import gr.uni.cinema.cinemamanagement.exception.InvalidProgramStateException;
import gr.uni.cinema.cinemamanagement.repository.ProgramRepository;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
public class ProgramServiceImpl implements ProgramService {

    private final ProgramRepository programRepository;

    public ProgramServiceImpl(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    @Override
    public Program createProgram(String name, String description, User creator) {

        if (creator.getRole() == UserRole.VISITOR) {
            throw new RuntimeException("Visitors cannot create programs");
        }

        if (programRepository.existsByName(name)) {
            throw new RuntimeException("Program name already exists");
        }

        Program program = new Program();
        program.setName(name);
        program.setDescription(description);
        program.setCreator(creator);
        program.setState(ProgramState.CREATED);

        return programRepository.save(program);
    }

    @Override
    public Program updateProgram(Long programId, String name, String description, User user) {

        Program program = getProgramById(programId);

        if (program.getState() == ProgramState.ANNOUNCED) {
            throw new RuntimeException("Cannot update announced program");
        }

        if (!program.getCreator().getId().equals(user.getId())
                && user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Not authorized to update program");
        }

        program.setName(name);
        program.setDescription(description);

        return programRepository.save(program);
    }

    @Override
    public Program changeState(Long programId, ProgramState newState, User user) {

        // 🔐 ROLE CHECK — ΠΡΩΤΟ
        if (user.getRole() != UserRole.PROGRAMMER) {
            throw new ForbiddenOperationException(
                    "Only programmers can change program state"
            );
        }

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Program not found"));

        // 🔄 STATE TRANSITION CHECK
        if (!ProgramStateRules.canTransition(program.getState(), newState)) {
            throw new InvalidProgramStateException(
                    "Cannot change state from " + program.getState() + " to " + newState
            );
        }

        program.setState(newState);
        return programRepository.save(program);
    }



    @Override
    public List<Program> getProgramsByState(ProgramState state) {
        return programRepository.findByState(state);
    }

    @Override
    public Program getProgramById(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Program not found"));
    }

    private boolean isValidTransition(ProgramState current, ProgramState next) {
        return switch (current) {
            case CREATED -> next == ProgramState.SUBMISSION;
            case SUBMISSION -> next == ProgramState.ASSIGNMENT;
            case ASSIGNMENT -> next == ProgramState.REVIEW;
            case REVIEW -> next == ProgramState.SCHEDULING;
            case SCHEDULING -> next == ProgramState.FINAL_PUBLICATION;
            case FINAL_PUBLICATION -> next == ProgramState.DECISION;
            case DECISION -> next == ProgramState.ANNOUNCED;
            default -> false;
        };
    }

    @Override
    public List<Program> getAllPrograms() {
        return programRepository.findAll();
    }

}
