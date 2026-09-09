package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.entity.*;
import gr.uni.cinema.cinemamanagement.exception.*;
import gr.uni.cinema.cinemamanagement.repository.ProgramRepository;
import gr.uni.cinema.cinemamanagement.repository.ProgramRoleRepository;
import gr.uni.cinema.cinemamanagement.repository.ScreeningRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;
import java.time.LocalDate;

@Service
public class ProgramServiceImpl implements ProgramService {

    private final ProgramRepository programRepository;
    private final ProgramRoleRepository programRoleRepository;
    private final ScreeningRepository screeningRepository;

    public ProgramServiceImpl(
            ProgramRepository programRepository,
            ProgramRoleRepository programRoleRepository,
            ScreeningRepository screeningRepository) {

        this.programRepository = programRepository;
        this.programRoleRepository = programRoleRepository;
        this.screeningRepository = screeningRepository;
    }


    // Dimiourgia program

    @Override
    @Transactional
    public Program createProgram(
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            User creator) {

        // O user prepei na einai authenticated.
        if (creator == null) {
            throw new ForbiddenOperationException(
                    "Authentication required to create a program"
            );
        }

        if (name == null || name.isBlank()) {
            throw new InvalidUserDataException(
                    "Program name is required"
            );
        }

        if (description == null || description.isBlank()) {
            throw new InvalidUserDataException(
                    "Program description is required"
            );
        }

        if (startDate == null || endDate == null) {
            throw new InvalidUserDataException(
                    "Program start date and end date are required"
            );
        }

        if (endDate.isBefore(startDate)) {
            throw new InvalidUserDataException(
                    "End date cannot be before start date"
            );
        }

        // To onoma tou program prepei na einai monodiko.
        if (programRepository.existsByName(name)) {
            throw new ProgramNameAlreadyExistsException(
                    "Program name already exists"
            );
        }

        Program program = new Program();
        program.setName(name);
        program.setDescription(description);
        program.setStartDate(startDate);
        program.setEndDate(endDate);
        program.setCreator(creator);
        program.setState(ProgramState.CREATED);

        // Apothikevoume prota to program.
        Program savedProgram = programRepository.save(program);

        // O creator ginetai automata PROGRAMMER tou program.
        ProgramRole programmerRole = new ProgramRole(
                creator,
                savedProgram,
                ProgramRoleType.PROGRAMMER
        );

        programRoleRepository.save(programmerRole);

        return savedProgram;
    }


    // Update program
    // Mono PROGRAMMER tou sygkekrimenou program.

    @Override
    @Transactional
    public Program updateProgram(
            Long programId,
            String name,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            User user) {

        Program program = getProgramById(programId);

        if (program.getState() == ProgramState.ANNOUNCED) {
            throw new InvalidProgramStateException(
                    "Announced program cannot be updated"
            );
        }

        boolean isProgrammer =
                programRoleRepository.existsByUserAndProgramAndRole(
                        user,
                        program,
                        ProgramRoleType.PROGRAMMER
                );

        if (!isProgrammer) {
            throw new ForbiddenOperationException(
                    "Only programmers of this program can update the program"
            );
        }

        if (name == null || name.isBlank()) {
            throw new InvalidProgramStateException(
                    "Program name is required"
            );
        }

        if (!program.getName().equals(name)
                && programRepository.existsByName(name)) {

            throw new ProgramNameAlreadyExistsException(
                    "Program name already exists"
            );
        }

        if ((startDate == null) != (endDate == null)) {
            throw new InvalidProgramStateException(
                    "Start date and end date must both be provided"
            );
        }

        if (startDate != null
                && endDate != null
                && endDate.isBefore(startDate)) {

            throw new InvalidProgramStateException(
                    "End date cannot be before start date"
            );
        }

        program.setName(name);
        program.setDescription(description);
        program.setStartDate(startDate);
        program.setEndDate(endDate);

        return programRepository.save(program);
    }


    // Delete program
    // Epitrepetai mono sto CREATED kai mono apo PROGRAMMER.

    @Override
    @Transactional
    public void deleteProgram(
            Long programId,
            User user) {

        Program program = getProgramById(programId);

        if (program.getState() != ProgramState.CREATED) {
            throw new InvalidProgramStateException(
                    "Program can only be deleted during CREATED phase"
            );
        }

        boolean isProgrammer =
                programRoleRepository.existsByUserAndProgramAndRole(
                        user,
                        program,
                        ProgramRoleType.PROGRAMMER
                );

        if (!isProgrammer) {
            throw new ForbiddenOperationException(
                    "Only programmers of this program can delete the program"
            );
        }

        programRoleRepository.deleteByProgram(program);
        programRepository.delete(program);
    }


    // Allagi state tou program.

    @Override
    @Transactional
    public Program changeState(
            Long programId,
            ProgramState newState,
            User user) {

        Program program = programRepository.findById(programId)
                .orElseThrow(() ->
                        new ProgramNotFoundException("Program not found")
                );

        // O user prepei na einai PROGRAMMER tou program.
        boolean isProgrammer =
                programRoleRepository.existsByUserAndProgramAndRole(
                        user,
                        program,
                        ProgramRoleType.PROGRAMMER
                );

        if (!isProgrammer) {
            throw new ForbiddenOperationException(
                    "Only programmers of this program can change program state"
            );
        }

        // Elegxos egkiris metavasi state.
        if (!ProgramStateRules.canTransition(
                program.getState(),
                newState)) {

            throw new InvalidProgramStateException(
                    "Cannot change state from "
                            + program.getState()
                            + " to "
                            + newState
            );
        }

        // Sto DECISION, APPROVED screenings xoris final submission
        // ginontai automata REJECTED.
        if (newState == ProgramState.DECISION) {

            List<Screening> notFinallySubmitted =
                    screeningRepository
                            .findByProgramIdAndStateAndFinalSubmittedFalse(
                                    program.getId(),
                                    ScreeningState.APPROVED
                            );

            for (Screening screening : notFinallySubmitted) {
                screening.setState(ScreeningState.REJECTED);
                screening.setRejectionReason(
                        "Automatically rejected because final submission was not completed"
                );
            }

            screeningRepository.saveAll(notFinallySubmitted);
        }

        program.setState(newState);

        return programRepository.save(program);
    }


    // Prostheti PROGRAMMER i STAFF sto program.

    @Override
    @Transactional
    public ProgramRole addRole(
            Long programId,
            User targetUser,
            ProgramRoleType role,
            User requestingUser) {

        Program program = programRepository.findById(programId)
                .orElseThrow(() ->
                        new ProgramNotFoundException("Program not found")
                );

        // STAFF mporei na prostethei mono se CREATED i SUBMISSION.
        if (role == ProgramRoleType.STAFF
                && program.getState() != ProgramState.CREATED
                && program.getState() != ProgramState.SUBMISSION) {

            throw new InvalidProgramStateException(
                    "STAFF can only be assigned during CREATED or SUBMISSION phase"
            );
        }

        // PROGRAMMER den mporei na prostethei meta to ANNOUNCED.
        if (role == ProgramRoleType.PROGRAMMER
                && program.getState() == ProgramState.ANNOUNCED) {

            throw new InvalidProgramStateException(
                    "PROGRAMMER cannot be assigned after the program is announced"
            );
        }

        // O requesting user prepei na einai PROGRAMMER tou program.
        boolean isProgrammer =
                programRoleRepository.existsByUserAndProgramAndRole(
                        requestingUser,
                        program,
                        ProgramRoleType.PROGRAMMER
                );

        if (!isProgrammer) {
            throw new ForbiddenOperationException(
                    "Only programmers of this program can assign roles"
            );
        }

        if (targetUser == null) {
            throw new ForbiddenOperationException(
                    "Target user is required"
            );
        }

        // O target user prepei na einai energos.
        if (!targetUser.isActive()) {
            throw new ForbiddenOperationException(
                    "Inactive users cannot be assigned to a program"
            );
        }

        // Mono USER mporei na parei program-specific role.
        if (targetUser.getRole() != UserRole.USER) {
            throw new ForbiddenOperationException(
                    "Only users with USER role can be assigned to a program"
            );
        }

        // Mono PROGRAMMER i STAFF anatithentai xeirokinita.
        if (role != ProgramRoleType.PROGRAMMER
                && role != ProgramRoleType.STAFF) {

            throw new ForbiddenOperationException(
                    "Only PROGRAMMER or STAFF role can be assigned"
            );
        }

        // O user mporei na exei mono ena role sto idio program.
        if (programRoleRepository
                .findByUserAndProgram(targetUser, program)
                .isPresent()) {

            throw new ForbiddenOperationException(
                    "User already has a role in this program"
            );
        }

        ProgramRole programRole = new ProgramRole(
                targetUser,
                program,
                role
        );

        return programRoleRepository.save(programRole);
    }


    // Anazitisi programs me visibility kai optional filters.

    @Override
    public List<Program> searchPrograms(
            String name,
            LocalDate fromDate,
            LocalDate toDate,
            User currentUser) {

        return programRepository.findAll()
                .stream()

                // Anonymous user vlepei mono ANNOUNCED programs.
                // Authenticated user vlepei kai programs sta opoia exei role.
                .filter(program -> {

                    if (program.getState() == ProgramState.ANNOUNCED) {
                        return true;
                    }

                    if (currentUser == null) {
                        return false;
                    }

                    return programRoleRepository
                            .findByUserAndProgram(
                                    currentUser,
                                    program
                            )
                            .isPresent();
                })

                // Optional filtro me onoma.
                .filter(program ->
                        name == null
                                || name.isBlank()
                                || program.getName()
                                .toLowerCase()
                                .contains(name.toLowerCase())
                )

                // Optional filtro apo imerominia.
                .filter(program ->
                        fromDate == null
                                || !program.getStartDate()
                                .isBefore(fromDate)
                )

                // Optional filtro mexri imerominia.
                .filter(program ->
                        toDate == null
                                || !program.getStartDate()
                                .isAfter(toDate)
                )

                // Taxinomisi prota me imerominia kai meta me onoma.
                .sorted(
                        Comparator
                                .comparing(
                                        Program::getStartDate
                                )
                                .thenComparing(
                                        Program::getName,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                )

                .toList();
    }


    // Epistrofi programs me sygkekrimeno state.

    @Override
    public List<Program> getProgramsByState(
            ProgramState state) {

        return programRepository.findByState(state);
    }


    // Evresi program me ID.

    @Override
    public Program getProgramById(Long id) {

        return programRepository.findById(id)
                .orElseThrow(() ->
                        new ProgramNotFoundException("Program not found")
                );
    }


    // Epistrofi olon ton programs.

    @Override
    public List<Program> getAllPrograms() {

        return programRepository.findAll();
    }


    // Elegxos visibility enos program.

    @Override
    public Program getVisibleProgramById(
            Long id,
            User currentUser) {

        Program program = getProgramById(id);

        // Ta ANNOUNCED programs einai public.
        if (program.getState() == ProgramState.ANNOUNCED) {
            return program;
        }

        // Anonymous user den vlepei non-public programs.
        if (currentUser == null) {
            throw new ForbiddenOperationException(
                    "This program is not publicly available"
            );
        }

        // Gia non-public program o user prepei na exei role.
        boolean hasRole =
                programRoleRepository
                        .findByUserAndProgram(
                                currentUser,
                                program
                        )
                        .isPresent();

        if (!hasRole) {
            throw new ForbiddenOperationException(
                    "You do not have access to this program"
            );
        }

        return program;
    }
}