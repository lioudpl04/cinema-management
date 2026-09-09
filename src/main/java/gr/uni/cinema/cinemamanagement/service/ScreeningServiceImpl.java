package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.entity.*;
import gr.uni.cinema.cinemamanagement.exception.InvalidScreeningStateException;
import gr.uni.cinema.cinemamanagement.exception.ScreeningAccessDeniedException;
import gr.uni.cinema.cinemamanagement.repository.ProgramRepository;
import gr.uni.cinema.cinemamanagement.repository.ProgramRoleRepository;
import gr.uni.cinema.cinemamanagement.repository.ScreeningRepository;
import gr.uni.cinema.cinemamanagement.exception.ScreeningNotFoundException;
import gr.uni.cinema.cinemamanagement.exception.ProgramNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScreeningServiceImpl implements ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;
    private final ProgramRoleRepository programRoleRepository;

    public ScreeningServiceImpl(
            ScreeningRepository screeningRepository,
            ProgramRepository programRepository,
            ProgramRoleRepository programRoleRepository) {

        this.screeningRepository = screeningRepository;
        this.programRepository = programRepository;
        this.programRoleRepository = programRoleRepository;
    }


    // Dimiourgia screening

    @Override
    @Transactional
    public Screening createScreening(
            String movieTitle,
            String cast,
            String genres,
            Integer durationMinutes,
            Long programId,
            User submitter) {

        // O submitter prepei na einai authenticated USER.
        if (submitter == null) {
            throw new ScreeningAccessDeniedException(
                    "Authentication required to create a screening"
            );
        }

        if (submitter.getRole() != UserRole.USER) {
            throw new ScreeningAccessDeniedException(
                    "Only users can submit screenings"
            );
        }

        if (!submitter.isActive()) {
            throw new ScreeningAccessDeniedException(
                    "Inactive users cannot submit screenings"
            );
        }

        Program program = programRepository.findById(programId)
                .orElseThrow(() ->
                        new ProgramNotFoundException("Program not found")
                );

        // Screening dimiourgeitai mono sti SUBMISSION fasi.
        if (program.getState() != ProgramState.SUBMISSION) {
            throw new InvalidScreeningStateException(
                    "Screenings can only be created during SUBMISSION phase"
            );
        }

        ProgramRole existingRole =
                programRoleRepository
                        .findByUserAndProgram(submitter, program)
                        .orElse(null);

        // O user den mporei na exei allo role sto idio program.
        if (existingRole != null
                && existingRole.getRole() != ProgramRoleType.SUBMITTER) {

            throw new ScreeningAccessDeniedException(
                    "User already has another role in this program"
            );
        }

        Screening screening = Screening.builder()
                .movieTitle(movieTitle)
                .cast(cast)
                .genres(genres)
                .durationMinutes(durationMinutes)
                .program(program)
                .submitter(submitter)
                .state(ScreeningState.CREATED)
                .build();

        Screening savedScreening =
                screeningRepository.save(screening);

        // Sto proto screening o user ginetai automata SUBMITTER tou program.
        if (programRoleRepository
                .findByUserAndProgram(submitter, program)
                .isEmpty()) {

            ProgramRole submitterRole = new ProgramRole(
                    submitter,
                    program,
                    ProgramRoleType.SUBMITTER
            );

            programRoleRepository.save(submitterRole);
        }

        return savedScreening;
    }


    // Allagi state tou screening

    @Override
    public Screening changeState(
            Long screeningId,
            ScreeningState newState,
            User user) {

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() ->
                        new ScreeningNotFoundException("Screening not found")
                );

        // Submit ginetai mono sti SUBMISSION fasi.
        if (newState == ScreeningState.SUBMITTED
                && screening.getProgram().getState() != ProgramState.SUBMISSION) {

            throw new InvalidScreeningStateException(
                    "Screenings can only be submitted during SUBMISSION phase"
            );
        }

        // Prin to submit prepei na yparxoun ola ta aparaithta stoixeia.
        if (newState == ScreeningState.SUBMITTED) {

            if (screening.getMovieTitle() == null
                    || screening.getMovieTitle().isBlank()) {

                throw new InvalidScreeningStateException(
                        "Movie title is required before submission"
                );
            }

            if (screening.getCast() == null
                    || screening.getCast().isBlank()) {

                throw new InvalidScreeningStateException(
                        "Cast is required before submission"
                );
            }

            if (screening.getGenres() == null
                    || screening.getGenres().isBlank()) {

                throw new InvalidScreeningStateException(
                        "Genres are required before submission"
                );
            }

            if (screening.getDurationMinutes() == null
                    || screening.getDurationMinutes() <= 0) {

                throw new InvalidScreeningStateException(
                        "Valid movie duration is required before submission"
                );
            }

            if (screening.getAuditorium() == null
                    || screening.getAuditorium().isBlank()) {

                throw new InvalidScreeningStateException(
                        "Auditorium is required before submission"
                );
            }

            if (screening.getStartTime() == null) {
                throw new InvalidScreeningStateException(
                        "Start time is required before submission"
                );
            }

            if (screening.getEndTime() == null) {
                throw new InvalidScreeningStateException(
                        "End time is required before submission"
                );
            }

            if (!screening.getEndTime()
                    .isAfter(screening.getStartTime())) {

                throw new InvalidScreeningStateException(
                        "End time must be after start time"
                );
            }
        }

        // Approval ginetai mono sti SCHEDULING fasi.
        if (newState == ScreeningState.APPROVED
                && screening.getProgram().getState() != ProgramState.SCHEDULING) {

            throw new InvalidScreeningStateException(
                    "Screenings can only be approved during SCHEDULING phase"
            );
        }

        ScreeningState currentState = screening.getState();

        // Elegxos egkiris metavasi state.
        if (!ScreeningStateRules.canTransition(
                currentState,
                newState)) {

            throw new InvalidScreeningStateException(
                    "Cannot change state from "
                            + currentState
                            + " to "
                            + newState
            );
        }

        ProgramRole programRole = programRoleRepository
                .findByUserAndProgram(
                        user,
                        screening.getProgram()
                )
                .orElseThrow(() ->
                        new ScreeningAccessDeniedException(
                                "User has no role in this program"
                        )
                );

        // Elegxos an o program role epitrepei ti metavasi.
        if (!ScreeningRoleRules.canChangeState(
                programRole.getRole(),
                currentState,
                newState)) {

            throw new ScreeningAccessDeniedException(
                    "Role "
                            + programRole.getRole()
                            + " cannot change screening from "
                            + currentState
                            + " to "
                            + newState
            );
        }

        // O SUBMITTER mporei na allaksei mono dika tou screenings.
        if (programRole.getRole() == ProgramRoleType.SUBMITTER
                && !screening.getSubmitter().getId()
                .equals(user.getId())) {

            throw new ScreeningAccessDeniedException(
                    "Submitters can change only their own screenings"
            );
        }

        // Review mporei na kanei mono o assigned STAFF.
        if (currentState == ScreeningState.SUBMITTED
                && newState == ScreeningState.REVIEWED) {

            if (screening.getAssignedStaff() == null) {
                throw new ScreeningAccessDeniedException(
                        "Screening has no assigned staff"
                );
            }

            if (!screening.getAssignedStaff().getId().equals(user.getId())) {
                throw new ScreeningAccessDeniedException(
                        "Only the assigned staff can review this screening"
                );
            }
        }

        screening.setState(newState);

        return screeningRepository.save(screening);
    }


    // Anathesi STAFF se screening

    @Override
    public Screening assignStaff(
            Long screeningId,
            User staff,
            User programmer) {

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() ->
                        new ScreeningNotFoundException("Screening not found")
                );

        // Assignment ginetai mono sti ASSIGNMENT fasi.
        if (screening.getProgram().getState() != ProgramState.ASSIGNMENT) {
            throw new InvalidScreeningStateException(
                    "Staff can only be assigned during ASSIGNMENT phase"
            );
        }

        if (screening.getState() != ScreeningState.SUBMITTED) {
            throw new InvalidScreeningStateException(
                    "Only SUBMITTED screenings can be assigned to staff"
            );
        }

        // Mono PROGRAMMER tou program mporei na kanei assignment.
        boolean isProgrammer =
                programRoleRepository.existsByUserAndProgramAndRole(
                        programmer,
                        screening.getProgram(),
                        ProgramRoleType.PROGRAMMER
                );

        if (!isProgrammer) {
            throw new ScreeningAccessDeniedException(
                    "Only programmers of this program can assign staff"
            );
        }

        // O epilegmenos user prepei na einai STAFF tou program.
        boolean isStaff =
                programRoleRepository.existsByUserAndProgramAndRole(
                        staff,
                        screening.getProgram(),
                        ProgramRoleType.STAFF
                );

        if (!isStaff) {
            throw new ScreeningAccessDeniedException(
                    "Selected user is not staff of this program"
            );
        }

        if (!staff.isActive()) {
            throw new ScreeningAccessDeniedException(
                    "Inactive staff cannot be assigned"
            );
        }

        screening.setAssignedStaff(staff);

        return screeningRepository.save(screening);
    }


    // Review screening

    @Override
    public Screening review(
            Long screeningId,
            Integer score,
            String comments,
            User staff) {

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() ->
                        new ScreeningNotFoundException("Screening not found")
                );

        // Review ginetai mono sti REVIEW fasi.
        if (screening.getProgram().getState() != ProgramState.REVIEW) {
            throw new InvalidScreeningStateException(
                    "Screenings can only be reviewed during REVIEW phase"
            );
        }

        if (screening.getState() != ScreeningState.SUBMITTED) {
            throw new InvalidScreeningStateException(
                    "Only SUBMITTED screenings can be reviewed"
            );
        }

        if (screening.getAssignedStaff() == null) {
            throw new ScreeningAccessDeniedException(
                    "Screening has no assigned staff"
            );
        }

        // Mono o assigned STAFF mporei na kanei review.
        if (!screening.getAssignedStaff().getId().equals(staff.getId())) {
            throw new ScreeningAccessDeniedException(
                    "Only the assigned staff can review this screening"
            );
        }

        boolean isStaff =
                programRoleRepository.existsByUserAndProgramAndRole(
                        staff,
                        screening.getProgram(),
                        ProgramRoleType.STAFF
                );

        if (!isStaff) {
            throw new ScreeningAccessDeniedException(
                    "User is not staff of this program"
            );
        }

        if (comments == null || comments.isBlank()) {
            throw new InvalidScreeningStateException(
                    "Review comments are required"
            );
        }

        if (score == null || score < 1 || score > 10) {
            throw new InvalidScreeningStateException(
                    "Review score must be between 1 and 10"
            );
        }

        screening.setReviewComments(comments);
        screening.setReviewedAt(LocalDateTime.now());
        screening.setState(ScreeningState.REVIEWED);
        screening.setReviewScore(score);

        return screeningRepository.save(screening);
    }


    // Aposyrsi screening

    @Override
    @Transactional
    public void withdraw(
            Long screeningId,
            User submitter) {

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() ->
                        new ScreeningNotFoundException("Screening not found")
                );

        // Withdraw ginetai mono oso to screening einai CREATED sti SUBMISSION.
        if (screening.getState() != ScreeningState.CREATED
                || screening.getProgram().getState() != ProgramState.SUBMISSION) {

            throw new InvalidScreeningStateException(
                    "Screening can only be withdrawn during SUBMISSION"
            );
        }

        // Mono o idios submitter mporei na kanei withdraw.
        if (screening.getSubmitter() == null
                || !screening.getSubmitter().getId().equals(submitter.getId())) {

            throw new ScreeningAccessDeniedException(
                    "Only the submitter can withdraw this screening"
            );
        }

        boolean isSubmitter =
                programRoleRepository.existsByUserAndProgramAndRole(
                        submitter,
                        screening.getProgram(),
                        ProgramRoleType.SUBMITTER
                );

        if (!isSubmitter) {
            throw new ScreeningAccessDeniedException(
                    "User is not submitter of this program"
            );
        }

        screeningRepository.delete(screening);
    }


    // Aporripsi screening

    @Override
    public Screening reject(
            Long screeningId,
            String reason,
            User programmer) {

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() ->
                        new ScreeningNotFoundException("Screening not found")
                );

        ProgramState programState =
                screening.getProgram().getState();

        // Reject ginetai mono se SCHEDULING i DECISION.
        if (programState != ProgramState.SCHEDULING
                && programState != ProgramState.DECISION) {

            throw new InvalidScreeningStateException(
                    "Screenings can only be rejected during SCHEDULING or DECISION phase"
            );
        }

        if (programState == ProgramState.SCHEDULING
                && screening.getState() != ScreeningState.REVIEWED) {

            throw new InvalidScreeningStateException(
                    "Only REVIEWED screenings can be rejected during SCHEDULING"
            );
        }

        if (programState == ProgramState.DECISION
                && screening.getState() != ScreeningState.APPROVED) {

            throw new InvalidScreeningStateException(
                    "Only APPROVED screenings can be rejected during DECISION"
            );
        }

        // Mono PROGRAMMER tou program mporei na kanei reject.
        boolean isProgrammer =
                programRoleRepository.existsByUserAndProgramAndRole(
                        programmer,
                        screening.getProgram(),
                        ProgramRoleType.PROGRAMMER
                );

        if (!isProgrammer) {
            throw new ScreeningAccessDeniedException(
                    "Only programmers of this program can reject screenings"
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new InvalidScreeningStateException(
                    "Rejection reason is required"
            );
        }

        screening.setRejectionReason(reason);
        screening.setState(ScreeningState.REJECTED);

        return screeningRepository.save(screening);
    }


    // Teliko scheduling screening

    @Override
    public Screening schedule(
            Long screeningId,
            String auditorium,
            LocalDateTime startTime,
            LocalDateTime endTime,
            User user) {

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() ->
                        new ScreeningNotFoundException("Screening not found")
                );

        // Final scheduling ginetai mono sti DECISION fasi.
        if (screening.getProgram().getState() != ProgramState.DECISION) {
            throw new InvalidScreeningStateException(
                    "Screenings can only be finally scheduled during DECISION phase"
            );
        }

        if (screening.getState() != ScreeningState.APPROVED) {
            throw new InvalidScreeningStateException(
                    "Only APPROVED screenings can be scheduled"
            );
        }

        // Prepei na exei ginei final submission.
        if (!screening.isFinalSubmitted()) {
            throw new InvalidScreeningStateException(
                    "Only finally submitted screenings can be scheduled"
            );
        }

        boolean isProgrammer =
                programRoleRepository
                        .existsByUserAndProgramAndRole(
                                user,
                                screening.getProgram(),
                                ProgramRoleType.PROGRAMMER
                        );

        if (!isProgrammer) {
            throw new ScreeningAccessDeniedException(
                    "Only programmers of this program can schedule screenings"
            );
        }

        if (auditorium == null || auditorium.isBlank()) {
            throw new InvalidScreeningStateException(
                    "Auditorium is required"
            );
        }

        if (startTime == null || endTime == null) {
            throw new InvalidScreeningStateException(
                    "Start time and end time are required"
            );
        }

        if (!endTime.isAfter(startTime)) {
            throw new InvalidScreeningStateException(
                    "End time must be after start time"
            );
        }

        if (screening.getDurationMinutes() == null
                || screening.getDurationMinutes() <= 0) {

            throw new InvalidScreeningStateException(
                    "Screening has no valid movie duration"
            );
        }

        long scheduledMinutes =
                Duration.between(startTime, endTime).toMinutes();

        // To diastima prepei na kalyptei ti diarkeia tis tainias.
        if (scheduledMinutes < screening.getDurationMinutes()) {

            throw new InvalidScreeningStateException(
                    "Scheduled time must be at least "
                            + screening.getDurationMinutes()
                            + " minutes"
            );
        }

        screening.setAuditorium(auditorium);
        screening.setStartTime(startTime);
        screening.setEndTime(endTime);
        screening.setState(ScreeningState.SCHEDULED);

        return screeningRepository.save(screening);
    }


    // Update screening

    @Override
    public Screening updateScreening(
            Long screeningId,
            String movieTitle,
            String cast,
            String genres,
            Integer durationMinutes,
            String auditorium,
            LocalDateTime startTime,
            User submitter) {

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() ->
                        new ScreeningNotFoundException("Screening not found")
                );

        // Kanoniko edit kata ti SUBMISSION fasi.
        boolean isCreatedDuringSubmission =
                screening.getState() == ScreeningState.CREATED
                        && screening.getProgram().getState() == ProgramState.SUBMISSION;

        // Edit prin apo to final submission.
        boolean isFinalSubmissionEdit =
                screening.getProgram().getState() == ProgramState.FINAL_SUBMISSION
                        && screening.getState() == ScreeningState.APPROVED
                        && !screening.isFinalSubmitted();

        if (!isCreatedDuringSubmission && !isFinalSubmissionEdit) {
            throw new InvalidScreeningStateException(
                    "Screening can only be updated during SUBMISSION or before final submission"
            );
        }

        // Mono o idios submitter mporei na kanei update.
        if (screening.getSubmitter() == null
                || !screening.getSubmitter().getId().equals(submitter.getId())) {

            throw new ScreeningAccessDeniedException(
                    "Only the submitter can update this screening"
            );
        }

        boolean isSubmitter =
                programRoleRepository.existsByUserAndProgramAndRole(
                        submitter,
                        screening.getProgram(),
                        ProgramRoleType.SUBMITTER
                );

        if (!isSubmitter) {
            throw new ScreeningAccessDeniedException(
                    "User is not submitter of this program"
            );
        }

        if (movieTitle == null || movieTitle.isBlank()) {
            throw new InvalidScreeningStateException(
                    "Movie title is required"
            );
        }

        if (cast == null || cast.isBlank()) {
            throw new InvalidScreeningStateException(
                    "Cast is required"
            );
        }

        if (genres == null || genres.isBlank()) {
            throw new InvalidScreeningStateException(
                    "Genres are required"
            );
        }

        if (durationMinutes == null || durationMinutes <= 0) {
            throw new InvalidScreeningStateException(
                    "Duration must be greater than 0"
            );
        }

        if (auditorium == null || auditorium.isBlank()) {
            throw new InvalidScreeningStateException(
                    "Auditorium is required"
            );
        }

        if (startTime == null) {
            throw new InvalidScreeningStateException(
                    "Start time is required"
            );
        }

        screening.setMovieTitle(movieTitle);
        screening.setCast(cast);
        screening.setGenres(genres);
        screening.setDurationMinutes(durationMinutes);
        screening.setAuditorium(auditorium);
        screening.setStartTime(startTime);

        // To endTime ypologizetai automata apo startTime + duration.
        screening.setEndTime(
                startTime.plusMinutes(durationMinutes)
        );

        return screeningRepository.save(screening);
    }


    // Screenings pou mporei na dei o user

    @Override
    public List<Screening> getVisibleScreenings(User user) {

        if (user == null) {
            throw new ScreeningAccessDeniedException(
                    "Authentication required"
            );
        }

        // O ADMIN vlepei ola ta screenings.
        if (user.getRole() == UserRole.ADMIN) {
            return screeningRepository.findAll();
        }

        List<Screening> allScreenings = screeningRepository.findAll();

        return allScreenings.stream()
                .filter(screening -> {

                    Program program = screening.getProgram();

                    // O PROGRAMMER vlepei ola ta screenings tou program.
                    boolean isProgrammer =
                            programRoleRepository.existsByUserAndProgramAndRole(
                                    user,
                                    program,
                                    ProgramRoleType.PROGRAMMER
                            );

                    if (isProgrammer) {
                        return true;
                    }

                    // O STAFF vlepei mono ta screenings pou tou exoun anatethei.
                    boolean isStaff =
                            programRoleRepository.existsByUserAndProgramAndRole(
                                    user,
                                    program,
                                    ProgramRoleType.STAFF
                            );

                    if (isStaff
                            && screening.getAssignedStaff() != null
                            && screening.getAssignedStaff()
                            .getId()
                            .equals(user.getId())) {

                        return true;
                    }

                    // O SUBMITTER vlepei mono ta dika tou screenings.
                    boolean isSubmitter =
                            programRoleRepository.existsByUserAndProgramAndRole(
                                    user,
                                    program,
                                    ProgramRoleType.SUBMITTER
                            );

                    return isSubmitter
                            && screening.getSubmitter() != null
                            && screening.getSubmitter()
                            .getId()
                            .equals(user.getId());
                })
                .toList();
    }


    // Anazitisi sta screenings pou mporei na dei o user

    @Override
    public List<Screening> searchScreenings(
            String movieTitle,
            String cast,
            String genre,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            User user) {

        List<Screening> visibleScreenings =
                getVisibleScreenings(user);

        return visibleScreenings.stream()
                .filter(screening -> {

                    if (movieTitle != null
                            && !movieTitle.isBlank()
                            && !containsAllWords(
                            screening.getMovieTitle(),
                            movieTitle)) {

                        return false;
                    }

                    if (cast != null
                            && !cast.isBlank()
                            && !containsAllWords(
                            screening.getCast(),
                            cast)) {

                        return false;
                    }

                    if (genre != null
                            && !genre.isBlank()
                            && !containsAllWords(
                            screening.getGenres(),
                            genre)) {

                        return false;
                    }

                    if (fromDate != null) {

                        if (screening.getStartTime() == null
                                || screening.getStartTime()
                                .isBefore(fromDate)) {

                            return false;
                        }
                    }

                    if (toDate != null) {

                        if (screening.getStartTime() == null
                                || screening.getStartTime()
                                .isAfter(toDate)) {

                            return false;
                        }
                    }

                    return true;
                })
                // Taxinomisi me genre kai meta me movie title.
                .sorted(
                        java.util.Comparator
                                .comparing(
                                        Screening::getGenres,
                                        java.util.Comparator.nullsLast(
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                                )
                                .thenComparing(
                                        Screening::getMovieTitle,
                                        java.util.Comparator.nullsLast(
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                                )
                )
                .toList();
    }


    // Elegxei an to field periexei oles tis lexeis tis anazitisis.
    private boolean containsAllWords(
            String fieldValue,
            String searchValue) {

        if (fieldValue == null) {
            return false;
        }

        String field =
                fieldValue.toLowerCase();

        String[] words =
                searchValue
                        .toLowerCase()
                        .trim()
                        .split("\\s+");

        for (String word : words) {

            if (!field.contains(word)) {
                return false;
            }
        }

        return true;
    }


    // Epistrofi screening me elegxo visibility

    @Override
    public Screening getVisibleScreeningById(
            Long screeningId,
            User user) {

        if (user == null) {
            throw new ScreeningAccessDeniedException(
                    "Authentication required"
            );
        }

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() ->
                        new ScreeningNotFoundException("Screening not found")
                );

        // O ADMIN exei pliri prosvasi.
        if (user.getRole() == UserRole.ADMIN) {
            return screening;
        }

        Program program = screening.getProgram();

        boolean isProgrammer =
                programRoleRepository.existsByUserAndProgramAndRole(
                        user,
                        program,
                        ProgramRoleType.PROGRAMMER
                );

        if (isProgrammer) {
            return screening;
        }

        boolean isStaff =
                programRoleRepository.existsByUserAndProgramAndRole(
                        user,
                        program,
                        ProgramRoleType.STAFF
                );

        if (isStaff
                && screening.getAssignedStaff() != null
                && screening.getAssignedStaff()
                .getId()
                .equals(user.getId())) {

            return screening;
        }

        boolean isSubmitter =
                programRoleRepository.existsByUserAndProgramAndRole(
                        user,
                        program,
                        ProgramRoleType.SUBMITTER
                );

        if (isSubmitter
                && screening.getSubmitter() != null
                && screening.getSubmitter()
                .getId()
                .equals(user.getId())) {

            return screening;
        }

        throw new ScreeningAccessDeniedException(
                "You are not allowed to view this screening"
        );
    }


    // Public einai mono SCHEDULED screenings apo ANNOUNCED programs.

    @Override
    public List<Screening> getPublicScreenings() {

        return screeningRepository.findAll()
                .stream()
                .filter(screening ->
                        screening.getProgram() != null
                                && screening.getProgram().getState()
                                == ProgramState.ANNOUNCED
                                && screening.getState()
                                == ScreeningState.SCHEDULED
                )
                .sorted(
                        java.util.Comparator.comparing(
                                Screening::getStartTime,
                                java.util.Comparator.nullsLast(
                                        java.util.Comparator.naturalOrder()
                                )
                        )
                )
                .toList();
    }


    // Final submission screening

    @Override
    public Screening finalSubmit(
            Long screeningId,
            User submitter) {

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() ->
                        new ScreeningNotFoundException("Screening not found")
                );

        // Final submit ginetai mono sti FINAL_SUBMISSION fasi.
        if (screening.getProgram().getState()
                != ProgramState.FINAL_SUBMISSION) {

            throw new InvalidScreeningStateException(
                    "Final submission is allowed only during FINAL_SUBMISSION phase"
            );
        }

        if (screening.getState() != ScreeningState.APPROVED) {
            throw new InvalidScreeningStateException(
                    "Only APPROVED screenings can be finally submitted"
            );
        }

        // Mono o idios submitter mporei na kanei final submit.
        if (screening.getSubmitter() == null
                || !screening.getSubmitter()
                .getId()
                .equals(submitter.getId())) {

            throw new ScreeningAccessDeniedException(
                    "Only the submitter can finally submit this screening"
            );
        }

        boolean isSubmitter =
                programRoleRepository.existsByUserAndProgramAndRole(
                        submitter,
                        screening.getProgram(),
                        ProgramRoleType.SUBMITTER
                );

        if (!isSubmitter) {
            throw new ScreeningAccessDeniedException(
                    "User is not submitter of this program"
            );
        }

        if (screening.isFinalSubmitted()) {
            throw new InvalidScreeningStateException(
                    "Screening has already been finally submitted"
            );
        }

        screening.setFinalSubmitted(true);
        screening.setFinalSubmittedAt(LocalDateTime.now());

        return screeningRepository.save(screening);
    }


    // Egkrisi screening

    @Override
    @Transactional
    public Screening approve(
            Long screeningId,
            String approvalNotes,
            User submitter) {

        // To changeState kanei tous elegxous gia REVIEWED -> APPROVED.
        Screening screening = changeState(
                screeningId,
                ScreeningState.APPROVED,
                submitter
        );

        screening.setApprovalNotes(approvalNotes);

        return screeningRepository.save(screening);
    }
}