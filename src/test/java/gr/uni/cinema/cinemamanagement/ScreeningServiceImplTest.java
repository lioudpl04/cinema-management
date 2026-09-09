package gr.uni.cinema.cinemamanagement;

import gr.uni.cinema.cinemamanagement.entity.*;
import gr.uni.cinema.cinemamanagement.exception.InvalidScreeningStateException;
import gr.uni.cinema.cinemamanagement.exception.ScreeningAccessDeniedException;
import gr.uni.cinema.cinemamanagement.repository.ProgramRepository;
import gr.uni.cinema.cinemamanagement.repository.ProgramRoleRepository;
import gr.uni.cinema.cinemamanagement.repository.ScreeningRepository;
import gr.uni.cinema.cinemamanagement.service.ScreeningServiceImpl;
import gr.uni.cinema.cinemamanagement.entity.ProgramRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceImplTest {

    @Mock
    private ScreeningRepository screeningRepository;

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private ProgramRoleRepository programRoleRepository;

    private ScreeningServiceImpl screeningService;

    @BeforeEach
    void setUp() {
        screeningService = new ScreeningServiceImpl(
                screeningRepository,
                programRepository,
                programRoleRepository
        );
    }

    @Test
    void createScreeningCreatesScreeningInCreatedState() {

        User submitter = createUser(1L, "submitter");

        Program program = createProgram(
                1L,
                ProgramState.SUBMISSION
        );

        when(programRepository.findById(1L))
                .thenReturn(Optional.of(program));

        when(programRoleRepository
                .findByUserAndProgram(submitter, program))
                .thenReturn(Optional.empty());

        when(screeningRepository.save(any(Screening.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(programRoleRepository.save(any(ProgramRole.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Screening result = screeningService.createScreening(
                "Test Movie",
                "Actor One, Actor Two",
                "Drama",
                120,
                1L,
                submitter
        );

        assertEquals("Test Movie", result.getMovieTitle());
        assertEquals("Actor One, Actor Two", result.getCast());
        assertEquals("Drama", result.getGenres());
        assertEquals(120, result.getDurationMinutes());
        assertEquals(ScreeningState.CREATED, result.getState());
        assertSame(program, result.getProgram());
        assertSame(submitter, result.getSubmitter());

        verify(screeningRepository)
                .save(any(Screening.class));
    }

    @Test
    void createScreeningAutomaticallyAssignsSubmitterRole() {

        User submitter = createUser(1L, "submitter");

        Program program = createProgram(
                1L,
                ProgramState.SUBMISSION
        );

        when(programRepository.findById(1L))
                .thenReturn(Optional.of(program));

        when(programRoleRepository
                .findByUserAndProgram(submitter, program))
                .thenReturn(Optional.empty());

        when(screeningRepository.save(any(Screening.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(programRoleRepository.save(any(ProgramRole.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        screeningService.createScreening(
                "Test Movie",
                "Actor One",
                "Drama",
                120,
                1L,
                submitter
        );

        ArgumentCaptor<ProgramRole> roleCaptor =
                ArgumentCaptor.forClass(ProgramRole.class);

        verify(programRoleRepository)
                .save(roleCaptor.capture());

        ProgramRole role = roleCaptor.getValue();

        assertSame(submitter, role.getUser());
        assertSame(program, role.getProgram());
        assertEquals(
                ProgramRoleType.SUBMITTER,
                role.getRole()
        );
    }

    @Test
    void createScreeningRejectedOutsideSubmissionPhase() {

        User submitter = createUser(1L, "submitter");

        Program program = createProgram(
                1L,
                ProgramState.CREATED
        );

        when(programRepository.findById(1L))
                .thenReturn(Optional.of(program));

        assertThrows(
                InvalidScreeningStateException.class,
                () -> screeningService.createScreening(
                        "Test Movie",
                        "Actor One",
                        "Drama",
                        120,
                        1L,
                        submitter
                )
        );

        verify(screeningRepository, never())
                .save(any(Screening.class));
    }

    @Test
    void assignedStaffCanReviewScreening() {

        User staff = createUser(2L, "staff");

        Program program = createProgram(
                1L,
                ProgramState.REVIEW
        );

        Screening screening = createScreening(
                1L,
                program,
                ScreeningState.SUBMITTED
        );

        screening.setAssignedStaff(staff);

        when(screeningRepository.findById(1L))
                .thenReturn(Optional.of(screening));

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        staff,
                        program,
                        ProgramRoleType.STAFF
                ))
                .thenReturn(true);

        when(screeningRepository.save(any(Screening.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Screening result = screeningService.review(
                1L,
                8,
                "Very good screening",
                staff
        );

        assertEquals(
                ScreeningState.REVIEWED,
                result.getState()
        );

        assertEquals(
                "Very good screening",
                result.getReviewComments()
        );

        assertEquals(
                8,
                result.getReviewScore()
        );

        assertNotNull(result.getReviewedAt());
    }

    @Test
    void reviewRejectsInvalidScore() {

        User staff = createUser(2L, "staff");

        Program program = createProgram(
                1L,
                ProgramState.REVIEW
        );

        Screening screening = createScreening(
                1L,
                program,
                ScreeningState.SUBMITTED
        );

        screening.setAssignedStaff(staff);

        when(screeningRepository.findById(1L))
                .thenReturn(Optional.of(screening));

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        staff,
                        program,
                        ProgramRoleType.STAFF
                ))
                .thenReturn(true);

        assertThrows(
                InvalidScreeningStateException.class,
                () -> screeningService.review(
                        1L,
                        11,
                        "Invalid score",
                        staff
                )
        );

        verify(screeningRepository, never())
                .save(any(Screening.class));
    }

    @Test
    void scheduleRejectsTimeShorterThanMovieDuration() {

        User programmer = createUser(1L, "programmer");

        Program program = createProgram(
                1L,
                ProgramState.DECISION
        );

        Screening screening = createScreening(
                1L,
                program,
                ScreeningState.APPROVED
        );

        screening.setFinalSubmitted(true);
        screening.setDurationMinutes(120);

        when(screeningRepository.findById(1L))
                .thenReturn(Optional.of(screening));

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        programmer,
                        program,
                        ProgramRoleType.PROGRAMMER
                ))
                .thenReturn(true);

        LocalDateTime start =
                LocalDateTime.of(
                        2026, 10, 10,
                        18, 0
                );

        LocalDateTime end =
                LocalDateTime.of(
                        2026, 10, 10,
                        19, 0
                );

        assertThrows(
                InvalidScreeningStateException.class,
                () -> screeningService.schedule(
                        1L,
                        "Hall A",
                        start,
                        end,
                        programmer
                )
        );

        verify(screeningRepository, never())
                .save(any(Screening.class));
    }

    @Test
    void programmerCanScheduleApprovedScreening() {

        User programmer = createUser(1L, "programmer");

        Program program = createProgram(
                1L,
                ProgramState.DECISION
        );

        Screening screening = createScreening(
                1L,
                program,
                ScreeningState.APPROVED
        );

        screening.setFinalSubmitted(true);
        screening.setDurationMinutes(120);

        when(screeningRepository.findById(1L))
                .thenReturn(Optional.of(screening));

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        programmer,
                        program,
                        ProgramRoleType.PROGRAMMER
                ))
                .thenReturn(true);

        when(screeningRepository.save(any(Screening.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime start =
                LocalDateTime.of(
                        2026, 10, 10,
                        18, 0
                );

        LocalDateTime end =
                LocalDateTime.of(
                        2026, 10, 10,
                        20, 0
                );

        Screening result = screeningService.schedule(
                1L,
                "Hall A",
                start,
                end,
                programmer
        );

        assertEquals(
                ScreeningState.SCHEDULED,
                result.getState()
        );

        assertEquals(
                "Hall A",
                result.getAuditorium()
        );

        assertEquals(start, result.getStartTime());
        assertEquals(end, result.getEndTime());
    }

    @Test
    void submitterCanFinallySubmitOwnApprovedScreening() {

        User submitter = createUser(1L, "submitter");

        Program program = createProgram(
                1L,
                ProgramState.FINAL_SUBMISSION
        );

        Screening screening = createScreening(
                1L,
                program,
                ScreeningState.APPROVED
        );

        screening.setSubmitter(submitter);
        screening.setFinalSubmitted(false);

        when(screeningRepository.findById(1L))
                .thenReturn(Optional.of(screening));

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        submitter,
                        program,
                        ProgramRoleType.SUBMITTER
                ))
                .thenReturn(true);

        when(screeningRepository.save(any(Screening.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Screening result =
                screeningService.finalSubmit(
                        1L,
                        submitter
                );

        assertTrue(result.isFinalSubmitted());
        assertNotNull(result.getFinalSubmittedAt());

        verify(screeningRepository)
                .save(screening);
    }


    // =========================================================
    // HELPER METHODS
    // =========================================================

    private User createUser(
            Long id,
            String username) {

        User user = new User();

        user.setId(id);
        user.setUsername(username);
        user.setFullName(username);
        user.setRole(UserRole.USER);
        user.setActive(true);

        return user;
    }

    private Program createProgram(
            Long id,
            ProgramState state) {

        Program program = new Program();

        program.setId(id);
        program.setName("Test Program");
        program.setState(state);

        return program;
    }

    private Screening createScreening(
            Long id,
            Program program,
            ScreeningState state) {

        Screening screening = new Screening();

        screening.setId(id);
        screening.setMovieTitle("Test Movie");
        screening.setProgram(program);
        screening.setState(state);

        return screening;
    }

    @Test
    void approvedScreeningCanBeUpdatedDuringFinalSubmissionBeforeFinalSubmit() {

        Program program = new Program();
        program.setId(1L);
        program.setState(ProgramState.FINAL_SUBMISSION);

        User submitter = new User();
        submitter.setId(10L);

        Screening screening = Screening.builder()
                .id(100L)
                .program(program)
                .submitter(submitter)
                .state(ScreeningState.APPROVED)
                .finalSubmitted(false)
                .build();

        when(screeningRepository.findById(100L))
                .thenReturn(Optional.of(screening));

        when(programRoleRepository.existsByUserAndProgramAndRole(
                submitter,
                program,
                ProgramRoleType.SUBMITTER))
                .thenReturn(true);

        when(screeningRepository.save(any(Screening.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Screening result = screeningService.updateScreening(
                100L,
                "Updated Movie",
                "Actor One",
                "Drama",
                120,
                "Hall A",
                LocalDateTime.of(2026, 10, 10, 18, 0),
                submitter
        );

        assertEquals("Updated Movie", result.getMovieTitle());
        assertEquals(ScreeningState.APPROVED, result.getState());
        assertEquals(
                LocalDateTime.of(2026, 10, 10, 20, 0),
                result.getEndTime()
        );
    }

    @Test
    void finallySubmittedScreeningCannotBeUpdated() {

        Program program = new Program();
        program.setId(1L);
        program.setState(ProgramState.FINAL_SUBMISSION);

        User submitter = new User();
        submitter.setId(10L);

        Screening screening = Screening.builder()
                .id(100L)
                .program(program)
                .submitter(submitter)
                .state(ScreeningState.APPROVED)
                .finalSubmitted(true)
                .build();

        when(screeningRepository.findById(100L))
                .thenReturn(Optional.of(screening));

        assertThrows(
                InvalidScreeningStateException.class,
                () -> screeningService.updateScreening(
                        100L,
                        "Updated Movie",
                        "Actor One",
                        "Drama",
                        120,
                        "Hall A",
                        LocalDateTime.of(2026, 10, 10, 18, 0),
                        submitter
                )
        );
    }

    @Test
    void approveStoresApprovalNotes() {

        Program program = new Program();
        program.setId(1L);
        program.setState(ProgramState.SCHEDULING);

        User submitter = new User();
        submitter.setId(10L);

        Screening screening = Screening.builder()
                .id(100L)
                .program(program)
                .submitter(submitter)
                .state(ScreeningState.REVIEWED)
                .build();

        when(screeningRepository.findById(100L))
                .thenReturn(Optional.of(screening));

        ProgramRole submitterRole = new ProgramRole();
        submitterRole.setUser(submitter);
        submitterRole.setProgram(program);
        submitterRole.setRole(ProgramRoleType.SUBMITTER);

        when(programRoleRepository.findByUserAndProgram(
                submitter,
                program
        )).thenReturn(Optional.of(submitterRole));

        when(screeningRepository.save(any(Screening.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Screening result = screeningService.approve(
                100L,
                "Fix the ending credits",
                submitter
        );

        assertEquals(ScreeningState.APPROVED, result.getState());
        assertEquals(
                "Fix the ending credits",
                result.getApprovalNotes()
        );
    }

    @Test
    void createdScreeningCannotBeUpdatedAfterSubmissionPhase() {

        Program program = new Program();
        program.setId(1L);
        program.setState(ProgramState.ASSIGNMENT);

        User submitter = new User();
        submitter.setId(10L);

        Screening screening = Screening.builder()
                .id(100L)
                .program(program)
                .submitter(submitter)
                .state(ScreeningState.CREATED)
                .build();

        when(screeningRepository.findById(100L))
                .thenReturn(Optional.of(screening));

        assertThrows(
                InvalidScreeningStateException.class,
                () -> screeningService.updateScreening(
                        100L,
                        "Movie",
                        "Actor",
                        "Drama",
                        120,
                        "Hall A",
                        LocalDateTime.of(2026, 10, 10, 18, 0),
                        submitter
                )
        );
    }

    @Test
    void createdScreeningCannotBeWithdrawnAfterSubmissionPhase() {

        Program program = new Program();
        program.setId(1L);
        program.setState(ProgramState.ASSIGNMENT);

        User submitter = new User();
        submitter.setId(10L);

        Screening screening = Screening.builder()
                .id(100L)
                .program(program)
                .submitter(submitter)
                .state(ScreeningState.CREATED)
                .build();

        when(screeningRepository.findById(100L))
                .thenReturn(Optional.of(screening));

        assertThrows(
                InvalidScreeningStateException.class,
                () -> screeningService.withdraw(
                        100L,
                        submitter
                )
        );

        verify(screeningRepository, never())
                .delete(screening);
    }

    @Test
    void approvedScreeningCannotBeScheduledWithoutFinalSubmission() {

        Program program = new Program();
        program.setId(1L);
        program.setState(ProgramState.DECISION);

        User programmer = new User();
        programmer.setId(10L);

        Screening screening = Screening.builder()
                .id(100L)
                .program(program)
                .state(ScreeningState.APPROVED)
                .finalSubmitted(false)
                .durationMinutes(120)
                .build();

        when(screeningRepository.findById(100L))
                .thenReturn(Optional.of(screening));

        assertThrows(
                InvalidScreeningStateException.class,
                () -> screeningService.schedule(
                        100L,
                        "Hall A",
                        LocalDateTime.of(2026, 10, 10, 18, 0),
                        LocalDateTime.of(2026, 10, 10, 20, 0),
                        programmer
                )
        );

        verify(screeningRepository, never())
                .save(any(Screening.class));
    }

    @Test
    void programmerCanRejectReviewedScreeningDuringScheduling() {

        Program program = new Program();
        program.setId(1L);
        program.setState(ProgramState.SCHEDULING);

        User programmer = new User();
        programmer.setId(10L);

        Screening screening = Screening.builder()
                .id(100L)
                .program(program)
                .state(ScreeningState.REVIEWED)
                .build();

        when(screeningRepository.findById(100L))
                .thenReturn(Optional.of(screening));

        when(programRoleRepository.existsByUserAndProgramAndRole(
                programmer,
                program,
                ProgramRoleType.PROGRAMMER
        )).thenReturn(true);

        when(screeningRepository.save(any(Screening.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Screening result = screeningService.reject(
                100L,
                "Review comments indicate rejection",
                programmer
        );

        assertEquals(
                ScreeningState.REJECTED,
                result.getState()
        );

        assertEquals(
                "Review comments indicate rejection",
                result.getRejectionReason()
        );
    }

    @Test
    void programmerCanRejectApprovedScreeningDuringDecision() {

        Program program = new Program();
        program.setId(1L);
        program.setState(ProgramState.DECISION);

        User programmer = new User();
        programmer.setId(10L);

        Screening screening = Screening.builder()
                .id(100L)
                .program(program)
                .state(ScreeningState.APPROVED)
                .finalSubmitted(true)
                .build();

        when(screeningRepository.findById(100L))
                .thenReturn(Optional.of(screening));

        when(programRoleRepository.existsByUserAndProgramAndRole(
                programmer,
                program,
                ProgramRoleType.PROGRAMMER
        )).thenReturn(true);

        when(screeningRepository.save(any(Screening.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Screening result = screeningService.reject(
                100L,
                "Required final changes were not completed",
                programmer
        );

        assertEquals(
                ScreeningState.REJECTED,
                result.getState()
        );

        assertEquals(
                "Required final changes were not completed",
                result.getRejectionReason()
        );
    }

    // =========================================================
// SEARCH / VISIBILITY / PUBLIC TIMETABLE
// =========================================================

    @Test
    void programmerCanSeeAllScreeningsOfOwnProgram() {

        User programmer = createUser(1L, "programmer");

        Program program = createProgram(
                1L,
                ProgramState.REVIEW
        );

        Screening first = createScreening(
                1L,
                program,
                ScreeningState.SUBMITTED
        );

        Screening second = createScreening(
                2L,
                program,
                ScreeningState.REVIEWED
        );

        when(screeningRepository.findAll())
                .thenReturn(List.of(first, second));

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        programmer,
                        program,
                        ProgramRoleType.PROGRAMMER
                ))
                .thenReturn(true);

        List<Screening> result =
                screeningService.getVisibleScreenings(
                        programmer
                );

        assertEquals(2, result.size());
        assertTrue(result.contains(first));
        assertTrue(result.contains(second));
    }


    @Test
    void assignedStaffCanSeeAssignedScreening() {

        User staff = createUser(2L, "staff");

        Program program = createProgram(
                1L,
                ProgramState.REVIEW
        );

        Screening screening = createScreening(
                1L,
                program,
                ScreeningState.SUBMITTED
        );

        screening.setAssignedStaff(staff);

        when(screeningRepository.findAll())
                .thenReturn(List.of(screening));

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        staff,
                        program,
                        ProgramRoleType.PROGRAMMER
                ))
                .thenReturn(false);

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        staff,
                        program,
                        ProgramRoleType.STAFF
                ))
                .thenReturn(true);

        List<Screening> result =
                screeningService.getVisibleScreenings(
                        staff
                );

        assertEquals(1, result.size());
        assertSame(screening, result.get(0));
    }


    @Test
    void staffCannotSeeScreeningAssignedToAnotherStaffMember() {

        User staff = createUser(2L, "staff");
        User otherStaff = createUser(3L, "otherstaff");

        Program program = createProgram(
                1L,
                ProgramState.REVIEW
        );

        Screening screening = createScreening(
                1L,
                program,
                ScreeningState.SUBMITTED
        );

        screening.setAssignedStaff(otherStaff);

        when(screeningRepository.findAll())
                .thenReturn(List.of(screening));

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        staff,
                        program,
                        ProgramRoleType.PROGRAMMER
                ))
                .thenReturn(false);

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        staff,
                        program,
                        ProgramRoleType.STAFF
                ))
                .thenReturn(true);

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        staff,
                        program,
                        ProgramRoleType.SUBMITTER
                ))
                .thenReturn(false);

        List<Screening> result =
                screeningService.getVisibleScreenings(
                        staff
                );

        assertTrue(result.isEmpty());
    }


    @Test
    void submitterCanSeeOnlyOwnScreening() {

        User submitter = createUser(1L, "submitter");
        User otherSubmitter = createUser(2L, "other");

        Program program = createProgram(
                1L,
                ProgramState.SUBMISSION
        );

        Screening own = createScreening(
                1L,
                program,
                ScreeningState.CREATED
        );
        own.setSubmitter(submitter);

        Screening other = createScreening(
                2L,
                program,
                ScreeningState.CREATED
        );
        other.setSubmitter(otherSubmitter);

        when(screeningRepository.findAll())
                .thenReturn(List.of(own, other));

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        submitter,
                        program,
                        ProgramRoleType.PROGRAMMER
                ))
                .thenReturn(false);

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        submitter,
                        program,
                        ProgramRoleType.STAFF
                ))
                .thenReturn(false);

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        submitter,
                        program,
                        ProgramRoleType.SUBMITTER
                ))
                .thenReturn(true);

        List<Screening> result =
                screeningService.getVisibleScreenings(
                        submitter
                );

        assertEquals(1, result.size());
        assertSame(own, result.get(0));
    }


    @Test
    void searchScreeningsUsesAllWordsCaseInsensitive() {

        User admin = createUser(1L, "admin");
        admin.setRole(UserRole.ADMIN);

        Program program = createProgram(
                1L,
                ProgramState.REVIEW
        );

        Screening matching = createScreening(
                1L,
                program,
                ScreeningState.REVIEWED
        );

        matching.setMovieTitle("The Great Cinema Story");
        matching.setCast("Actor One Actor Two");
        matching.setGenres("Science Fiction Drama");

        Screening other = createScreening(
                2L,
                program,
                ScreeningState.REVIEWED
        );

        other.setMovieTitle("Different Movie");
        other.setCast("Someone Else");
        other.setGenres("Comedy");

        when(screeningRepository.findAll())
                .thenReturn(List.of(matching, other));

        List<Screening> result =
                screeningService.searchScreenings(
                        "CINEMA great",
                        "actor TWO",
                        "fiction SCIENCE",
                        null,
                        null,
                        admin
                );

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }


    @Test
    void searchScreeningsAppliesAllFiltersWithAndSemantics() {

        User admin = createUser(1L, "admin");
        admin.setRole(UserRole.ADMIN);

        Program program = createProgram(
                1L,
                ProgramState.REVIEW
        );

        Screening matching = createScreening(
                1L,
                program,
                ScreeningState.REVIEWED
        );

        matching.setMovieTitle("Cinema Story");
        matching.setCast("Actor One");
        matching.setGenres("Drama");
        matching.setStartTime(
                LocalDateTime.of(
                        2026, 10, 15,
                        18, 0
                )
        );

        Screening wrongDate = createScreening(
                2L,
                program,
                ScreeningState.REVIEWED
        );

        wrongDate.setMovieTitle("Cinema Story");
        wrongDate.setCast("Actor One");
        wrongDate.setGenres("Drama");
        wrongDate.setStartTime(
                LocalDateTime.of(
                        2027, 1, 10,
                        18, 0
                )
        );

        when(screeningRepository.findAll())
                .thenReturn(List.of(
                        matching,
                        wrongDate
                ));

        List<Screening> result =
                screeningService.searchScreenings(
                        "Cinema",
                        "Actor",
                        "Drama",
                        LocalDateTime.of(
                                2026, 10, 1,
                                0, 0
                        ),
                        LocalDateTime.of(
                                2026, 10, 31,
                                23, 59
                        ),
                        admin
                );

        assertEquals(1, result.size());
        assertSame(matching, result.get(0));
    }


    @Test
    void searchScreeningsSortsByGenreThenMovieTitle() {

        User admin = createUser(1L, "admin");
        admin.setRole(UserRole.ADMIN);

        Program program = createProgram(
                1L,
                ProgramState.REVIEW
        );

        Screening dramaBeta = createScreening(
                1L,
                program,
                ScreeningState.REVIEWED
        );
        dramaBeta.setGenres("Drama");
        dramaBeta.setMovieTitle("Beta");

        Screening comedy = createScreening(
                2L,
                program,
                ScreeningState.REVIEWED
        );
        comedy.setGenres("Comedy");
        comedy.setMovieTitle("Cinema");

        Screening dramaAlpha = createScreening(
                3L,
                program,
                ScreeningState.REVIEWED
        );
        dramaAlpha.setGenres("Drama");
        dramaAlpha.setMovieTitle("Alpha");

        when(screeningRepository.findAll())
                .thenReturn(List.of(
                        dramaBeta,
                        comedy,
                        dramaAlpha
                ));

        List<Screening> result =
                screeningService.searchScreenings(
                        null,
                        null,
                        null,
                        null,
                        null,
                        admin
                );

        assertEquals(
                List.of(
                        comedy,
                        dramaAlpha,
                        dramaBeta
                ),
                result
        );
    }


    @Test
    void publicTimetableContainsOnlyScheduledScreeningsOfAnnouncedPrograms() {

        Program announced = createProgram(
                1L,
                ProgramState.ANNOUNCED
        );

        Program privateProgram = createProgram(
                2L,
                ProgramState.DECISION
        );

        Screening publicScreening = createScreening(
                1L,
                announced,
                ScreeningState.SCHEDULED
        );
        publicScreening.setStartTime(
                LocalDateTime.of(
                        2026, 10, 10,
                        20, 0
                )
        );

        Screening notScheduled = createScreening(
                2L,
                announced,
                ScreeningState.APPROVED
        );
        notScheduled.setStartTime(
                LocalDateTime.of(
                        2026, 10, 10,
                        18, 0
                )
        );

        Screening privateScreening = createScreening(
                3L,
                privateProgram,
                ScreeningState.SCHEDULED
        );
        privateScreening.setStartTime(
                LocalDateTime.of(
                        2026, 10, 10,
                        19, 0
                )
        );

        when(screeningRepository.findAll())
                .thenReturn(List.of(
                        publicScreening,
                        notScheduled,
                        privateScreening
                ));

        List<Screening> result =
                screeningService.getPublicScreenings();

        assertEquals(1, result.size());
        assertSame(publicScreening, result.get(0));
    }


    @Test
    void publicTimetableIsSortedByStartTime() {

        Program program = createProgram(
                1L,
                ProgramState.ANNOUNCED
        );

        Screening later = createScreening(
                1L,
                program,
                ScreeningState.SCHEDULED
        );
        later.setStartTime(
                LocalDateTime.of(
                        2026, 10, 10,
                        21, 0
                )
        );

        Screening earlier = createScreening(
                2L,
                program,
                ScreeningState.SCHEDULED
        );
        earlier.setStartTime(
                LocalDateTime.of(
                        2026, 10, 10,
                        18, 0
                )
        );

        when(screeningRepository.findAll())
                .thenReturn(List.of(
                        later,
                        earlier
                ));

        List<Screening> result =
                screeningService.getPublicScreenings();

        assertEquals(
                List.of(
                        earlier,
                        later
                ),
                result
        );
    }

}