package gr.uni.cinema.cinemamanagement;

import gr.uni.cinema.cinemamanagement.entity.*;
import gr.uni.cinema.cinemamanagement.exception.ForbiddenOperationException;
import gr.uni.cinema.cinemamanagement.exception.InvalidProgramStateException;
import gr.uni.cinema.cinemamanagement.exception.InvalidUserDataException;
import gr.uni.cinema.cinemamanagement.exception.ProgramNameAlreadyExistsException;
import gr.uni.cinema.cinemamanagement.repository.ProgramRepository;
import gr.uni.cinema.cinemamanagement.repository.ProgramRoleRepository;
import gr.uni.cinema.cinemamanagement.repository.ScreeningRepository;
import gr.uni.cinema.cinemamanagement.service.ProgramServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgramServiceImplTest {

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private ProgramRoleRepository programRoleRepository;

    @Mock
    private ScreeningRepository screeningRepository;

    private ProgramServiceImpl programService;

    @BeforeEach
    void setUp() {
        programService = new ProgramServiceImpl(
                programRepository,
                programRoleRepository,
                screeningRepository
        );
    }

    @Test
    void createProgramCreatesProgramInCreatedState() {

        User creator = createUser(1L, "creator");

        when(programRepository.existsByName("Festival"))
                .thenReturn(false);

        when(programRepository.save(any(Program.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(programRoleRepository.save(any(ProgramRole.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Program result = programService.createProgram(
                "Festival",
                "Cinema Festival",
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 20),
                creator
        );

        assertEquals("Festival", result.getName());
        assertEquals("Cinema Festival", result.getDescription());
        assertEquals(
                LocalDate.of(2026, 10, 10),
                result.getStartDate()
        );

        assertEquals(
                LocalDate.of(2026, 10, 20),
                result.getEndDate()
        );
        assertEquals(ProgramState.CREATED, result.getState());
        assertSame(creator, result.getCreator());

        verify(programRepository).save(any(Program.class));
    }

    @Test
    void createProgramAutomaticallyAssignsProgrammerRole() {

        User creator = createUser(1L, "creator");

        when(programRepository.existsByName("Festival"))
                .thenReturn(false);

        when(programRepository.save(any(Program.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(programRoleRepository.save(any(ProgramRole.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Program program = programService.createProgram(
                "Festival",
                "Cinema Festival",
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 20),
                creator
        );

        ArgumentCaptor<ProgramRole> roleCaptor =
                ArgumentCaptor.forClass(ProgramRole.class);

        verify(programRoleRepository).save(roleCaptor.capture());

        ProgramRole savedRole = roleCaptor.getValue();

        assertSame(creator, savedRole.getUser());
        assertSame(program, savedRole.getProgram());
        assertEquals(
                ProgramRoleType.PROGRAMMER,
                savedRole.getRole()
        );
    }

    @Test
    void createProgramRejectsDuplicateName() {

        User creator = createUser(1L, "creator");

        when(programRepository.existsByName("Festival"))
                .thenReturn(true);

        assertThrows(
                ProgramNameAlreadyExistsException.class,
                () -> programService.createProgram(
                        "Festival",
                        "Cinema Festival",
                        LocalDate.of(2026, 10, 10),
                        LocalDate.of(2026, 10, 20),
                        creator
                )
        );

        verify(programRepository, never())
                .save(any(Program.class));

        verify(programRoleRepository, never())
                .save(any(ProgramRole.class));
    }

    @Test
    void changeStateRejectsNonProgrammer() {

        User user = createUser(2L, "normaluser");

        Program program = createProgram(
                1L,
                ProgramState.CREATED
        );

        when(programRepository.findById(1L))
                .thenReturn(Optional.of(program));

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        user,
                        program,
                        ProgramRoleType.PROGRAMMER
                ))
                .thenReturn(false);

        assertThrows(
                ForbiddenOperationException.class,
                () -> programService.changeState(
                        1L,
                        ProgramState.SUBMISSION,
                        user
                )
        );

        verify(programRepository, never())
                .save(any(Program.class));
    }

    @Test
    void changeStateRejectsInvalidTransition() {

        User programmer = createUser(1L, "programmer");

        Program program = createProgram(
                1L,
                ProgramState.CREATED
        );

        when(programRepository.findById(1L))
                .thenReturn(Optional.of(program));

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        programmer,
                        program,
                        ProgramRoleType.PROGRAMMER
                ))
                .thenReturn(true);

        assertThrows(
                InvalidProgramStateException.class,
                () -> programService.changeState(
                        1L,
                        ProgramState.REVIEW,
                        programmer
                )
        );

        verify(programRepository, never())
                .save(any(Program.class));
    }

    @Test
    void programmerCanAssignStaffDuringCreated() {

        User programmer = createUser(1L, "programmer");
        User staff = createUser(2L, "staff");

        Program program = createProgram(
                1L,
                ProgramState.CREATED
        );

        when(programRepository.findById(1L))
                .thenReturn(Optional.of(program));

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        programmer,
                        program,
                        ProgramRoleType.PROGRAMMER
                ))
                .thenReturn(true);

        when(programRoleRepository
                .findByUserAndProgram(staff, program))
                .thenReturn(Optional.empty());

        when(programRoleRepository.save(any(ProgramRole.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProgramRole result = programService.addRole(
                1L,
                staff,
                ProgramRoleType.STAFF,
                programmer
        );

        assertSame(staff, result.getUser());
        assertSame(program, result.getProgram());
        assertEquals(
                ProgramRoleType.STAFF,
                result.getRole()
        );
    }

    @Test
    void cannotAssignRoleAfterSubmissionPhase() {

        User programmer = createUser(1L, "programmer");
        User staff = createUser(2L, "staff");

        Program program = createProgram(
                1L,
                ProgramState.ASSIGNMENT
        );

        when(programRepository.findById(1L))
                .thenReturn(Optional.of(program));

        assertThrows(
                InvalidProgramStateException.class,
                () -> programService.addRole(
                        1L,
                        staff,
                        ProgramRoleType.STAFF,
                        programmer
                )
        );

        verify(programRoleRepository, never())
                .save(any(ProgramRole.class));
    }

    @Test
    void enteringDecisionRejectsApprovedScreeningsWithoutFinalSubmission() {

        User programmer = createUser(1L, "programmer");

        Program program = createProgram(
                1L,
                ProgramState.FINAL_SUBMISSION
        );

        Screening screening = new Screening();
        screening.setState(ScreeningState.APPROVED);
        screening.setFinalSubmitted(false);

        when(programRepository.findById(1L))
                .thenReturn(Optional.of(program));

        when(programRoleRepository
                .existsByUserAndProgramAndRole(
                        programmer,
                        program,
                        ProgramRoleType.PROGRAMMER
                ))
                .thenReturn(true);

        when(screeningRepository
                .findByProgramIdAndStateAndFinalSubmittedFalse(
                        1L,
                        ScreeningState.APPROVED
                ))
                .thenReturn(List.of(screening));

        when(programRepository.save(any(Program.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Program result = programService.changeState(
                1L,
                ProgramState.DECISION,
                programmer
        );

        assertEquals(
                ProgramState.DECISION,
                result.getState()
        );

        assertEquals(
                ScreeningState.REJECTED,
                screening.getState()
        );

        assertNotNull(screening.getRejectionReason());

        verify(screeningRepository)
                .saveAll(anyList());
    }


    // =========================================================
    // HELPER METHODS
    // =========================================================

    private User createUser(Long id, String username) {

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

    @Test
    void createProgramWithBlankNameFails() {

        User creator = new User();
        creator.setId(1L);
        creator.setRole(UserRole.USER);
        creator.setActive(true);

        assertThrows(
                InvalidUserDataException.class,
                () -> programService.createProgram(
                        "   ",
                        "Description",
                        LocalDate.of(2026, 10, 10),
                        LocalDate.of(2026, 10, 20),
                        creator
                )
        );

        verify(programRepository, never())
                .save(any(Program.class));
    }

    @Test
    void createProgramWithNullNameFails() {

        User creator = new User();
        creator.setId(1L);
        creator.setRole(UserRole.USER);
        creator.setActive(true);

        assertThrows(
                InvalidUserDataException.class,
                () -> programService.createProgram(
                        null,
                        "Description",
                        LocalDate.of(2026, 10, 10),
                        LocalDate.of(2026, 10, 20),
                        creator
                )
        );

        verify(programRepository, never())
                .save(any(Program.class));
    }

    @Test
    void updateProgramFailsWhenEndDateIsBeforeStartDate() {

        User programmer = new User();
        programmer.setId(1L);

        Program program = new Program();
        program.setId(10L);
        program.setName("Cinema Festival");
        program.setState(ProgramState.CREATED);

        when(programRepository.findById(10L))
                .thenReturn(Optional.of(program));

        when(programRoleRepository.existsByUserAndProgramAndRole(
                programmer,
                program,
                ProgramRoleType.PROGRAMMER
        )).thenReturn(true);

        LocalDate startDate =
                LocalDate.of(2026, 10, 20);

        LocalDate endDate =
                LocalDate.of(2026, 10, 10);

        assertThrows(
                InvalidProgramStateException.class,
                () -> programService.updateProgram(
                        10L,
                        "Cinema Festival",
                        "Description",
                        startDate,
                        endDate,
                        programmer
                )
        );

        verify(programRepository, never())
                .save(any(Program.class));
    }

    @Test
    void updateProgramFailsWhenOnlyOneDateIsProvided() {

        User programmer = new User();
        programmer.setId(1L);

        Program program = new Program();
        program.setId(10L);
        program.setName("Cinema Festival");
        program.setState(ProgramState.CREATED);

        when(programRepository.findById(10L))
                .thenReturn(Optional.of(program));

        when(programRoleRepository.existsByUserAndProgramAndRole(
                programmer,
                program,
                ProgramRoleType.PROGRAMMER
        )).thenReturn(true);

        LocalDate startDate =
                LocalDate.of(2026, 10, 10);

        assertThrows(
                InvalidProgramStateException.class,
                () -> programService.updateProgram(
                        10L,
                        "Cinema Festival",
                        "Description",
                        startDate,
                        null,
                        programmer
                )
        );

        verify(programRepository, never())
                .save(any(Program.class));
    }

    @Test
    void programCanBeUpdatedBeforeAnnounced() {

        User programmer = new User();
        programmer.setId(1L);

        Program program = new Program();
        program.setId(10L);
        program.setName("Old Name");
        program.setState(ProgramState.REVIEW);

        when(programRepository.findById(10L))
                .thenReturn(Optional.of(program));

        when(programRoleRepository.existsByUserAndProgramAndRole(
                programmer,
                program,
                ProgramRoleType.PROGRAMMER
        )).thenReturn(true);

        when(programRepository.existsByName("New Name"))
                .thenReturn(false);

        when(programRepository.save(any(Program.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Program result = programService.updateProgram(
                10L,
                "New Name",
                "Updated description",
                LocalDate.of(2026, 10, 10),
                LocalDate.of(2026, 10, 20),
                programmer
        );

        assertEquals("New Name", result.getName());
        assertEquals(
                ProgramState.REVIEW,
                result.getState()
        );
    }

    @Test
    void createProgramFailsWhenDescriptionIsBlank() {

        User creator = new User();
        creator.setId(1L);
        creator.setRole(UserRole.USER);
        creator.setActive(true);

        assertThrows(
                InvalidUserDataException.class,
                () -> programService.createProgram(
                        "Festival",
                        "   ",
                        LocalDate.of(2026, 10, 10),
                        LocalDate.of(2026, 10, 20),
                        creator
                )
        );

        verify(programRepository, never())
                .save(any(Program.class));
    }

    @Test
    void createProgramFailsWhenDatesAreMissing() {

        User creator = new User();
        creator.setId(1L);
        creator.setRole(UserRole.USER);
        creator.setActive(true);

        assertThrows(
                InvalidUserDataException.class,
                () -> programService.createProgram(
                        "Festival",
                        "Cinema Festival",
                        null,
                        null,
                        creator
                )
        );

        verify(programRepository, never())
                .save(any(Program.class));
    }

    @Test
    void createProgramFailsWhenEndDateIsBeforeStartDate() {

        User creator = new User();
        creator.setId(1L);
        creator.setRole(UserRole.USER);
        creator.setActive(true);

        assertThrows(
                InvalidUserDataException.class,
                () -> programService.createProgram(
                        "Festival",
                        "Cinema Festival",
                        LocalDate.of(2026, 10, 20),
                        LocalDate.of(2026, 10, 10),
                        creator
                )
        );

        verify(programRepository, never())
                .save(any(Program.class));
    }

    @Test
    void programmerCanBeAssignedAfterSubmissionPhase() {

        User requestingProgrammer = createUser(1L, "programmer");
        User targetUser = createUser(2L, "newprogrammer");

        Program program = createProgram(
                1L,
                ProgramState.REVIEW
        );

        when(programRepository.findById(1L))
                .thenReturn(Optional.of(program));

        when(programRoleRepository.existsByUserAndProgramAndRole(
                requestingProgrammer,
                program,
                ProgramRoleType.PROGRAMMER
        )).thenReturn(true);

        when(programRoleRepository.findByUserAndProgram(
                targetUser,
                program
        )).thenReturn(Optional.empty());

        when(programRoleRepository.save(any(ProgramRole.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProgramRole result = programService.addRole(
                1L,
                targetUser,
                ProgramRoleType.PROGRAMMER,
                requestingProgrammer
        );

        assertSame(targetUser, result.getUser());
        assertSame(program, result.getProgram());
        assertEquals(
                ProgramRoleType.PROGRAMMER,
                result.getRole()
        );
    }

    @Test
    void programmerCannotBeAssignedAfterProgramIsAnnounced() {

        User requestingProgrammer = createUser(1L, "programmer");
        User targetUser = createUser(2L, "newprogrammer");

        Program program = createProgram(
                1L,
                ProgramState.ANNOUNCED
        );

        when(programRepository.findById(1L))
                .thenReturn(Optional.of(program));

        assertThrows(
                InvalidProgramStateException.class,
                () -> programService.addRole(
                        1L,
                        targetUser,
                        ProgramRoleType.PROGRAMMER,
                        requestingProgrammer
                )
        );

        verify(programRoleRepository, never())
                .save(any(ProgramRole.class));
    }

    // =========================================================
// PROGRAM SEARCH / VISIBILITY
// =========================================================

    @Test
    void anonymousSearchReturnsOnlyAnnouncedPrograms() {

        Program announced = createProgram(
                1L,
                ProgramState.ANNOUNCED
        );
        announced.setName("Public Festival");
        announced.setStartDate(
                LocalDate.of(2026, 10, 10)
        );

        Program created = createProgram(
                2L,
                ProgramState.CREATED
        );
        created.setName("Private Festival");
        created.setStartDate(
                LocalDate.of(2026, 11, 10)
        );

        when(programRepository.findAll())
                .thenReturn(List.of(
                        announced,
                        created
                ));

        List<Program> result =
                programService.searchPrograms(
                        null,
                        null,
                        null,
                        null
                );

        assertEquals(1, result.size());
        assertSame(announced, result.get(0));
    }


    @Test
    void authenticatedUserCanSeeProgramWhereTheyHaveRole() {

        User user = createUser(
                1L,
                "programmer"
        );

        Program program = createProgram(
                10L,
                ProgramState.REVIEW
        );

        program.setStartDate(
                LocalDate.of(2026, 10, 10)
        );

        when(programRepository.findAll())
                .thenReturn(List.of(program));

        ProgramRole role = new ProgramRole(
                user,
                program,
                ProgramRoleType.PROGRAMMER
        );

        when(programRoleRepository
                .findByUserAndProgram(
                        user,
                        program
                ))
                .thenReturn(Optional.of(role));

        List<Program> result =
                programService.searchPrograms(
                        null,
                        null,
                        null,
                        user
                );

        assertEquals(1, result.size());
        assertSame(program, result.get(0));
    }


    @Test
    void authenticatedUserCannotSeePrivateProgramWithoutRole() {

        User user = createUser(
                1L,
                "normaluser"
        );

        Program program = createProgram(
                10L,
                ProgramState.REVIEW
        );

        program.setStartDate(
                LocalDate.of(2026, 10, 10)
        );

        when(programRepository.findAll())
                .thenReturn(List.of(program));

        when(programRoleRepository
                .findByUserAndProgram(
                        user,
                        program
                ))
                .thenReturn(Optional.empty());

        List<Program> result =
                programService.searchPrograms(
                        null,
                        null,
                        null,
                        user
                );

        assertTrue(result.isEmpty());
    }


    @Test
    void searchProgramsFiltersByNameCaseInsensitive() {

        Program first = createProgram(
                1L,
                ProgramState.ANNOUNCED
        );

        first.setName("Cinema Festival");
        first.setStartDate(
                LocalDate.of(2026, 10, 10)
        );

        Program second = createProgram(
                2L,
                ProgramState.ANNOUNCED
        );

        second.setName("Documentary Week");
        second.setStartDate(
                LocalDate.of(2026, 11, 10)
        );

        when(programRepository.findAll())
                .thenReturn(List.of(
                        first,
                        second
                ));

        List<Program> result =
                programService.searchPrograms(
                        "FESTIVAL",
                        null,
                        null,
                        null
                );

        assertEquals(1, result.size());
        assertSame(first, result.get(0));
    }


    @Test
    void searchProgramsFiltersByDateRange() {

        Program inside = createProgram(
                1L,
                ProgramState.ANNOUNCED
        );

        inside.setName("Inside");
        inside.setStartDate(
                LocalDate.of(2026, 6, 15)
        );

        Program before = createProgram(
                2L,
                ProgramState.ANNOUNCED
        );

        before.setName("Before");
        before.setStartDate(
                LocalDate.of(2026, 1, 10)
        );

        Program after = createProgram(
                3L,
                ProgramState.ANNOUNCED
        );

        after.setName("After");
        after.setStartDate(
                LocalDate.of(2027, 1, 10)
        );

        when(programRepository.findAll())
                .thenReturn(List.of(
                        inside,
                        before,
                        after
                ));

        List<Program> result =
                programService.searchPrograms(
                        null,
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 12, 31),
                        null
                );

        assertEquals(1, result.size());
        assertSame(inside, result.get(0));
    }


    @Test
    void searchProgramsSortsByDateThenName() {

        Program beta = createProgram(
                1L,
                ProgramState.ANNOUNCED
        );
        beta.setName("Beta");
        beta.setStartDate(
                LocalDate.of(2026, 10, 10)
        );

        Program alpha = createProgram(
                2L,
                ProgramState.ANNOUNCED
        );
        alpha.setName("Alpha");
        alpha.setStartDate(
                LocalDate.of(2026, 10, 10)
        );

        Program earlier = createProgram(
                3L,
                ProgramState.ANNOUNCED
        );
        earlier.setName("Cinema");
        earlier.setStartDate(
                LocalDate.of(2026, 9, 1)
        );

        when(programRepository.findAll())
                .thenReturn(List.of(
                        beta,
                        alpha,
                        earlier
                ));

        List<Program> result =
                programService.searchPrograms(
                        null,
                        null,
                        null,
                        null
                );

        assertEquals(
                List.of(
                        earlier,
                        alpha,
                        beta
                ),
                result
        );
    }


// =========================================================
// SINGLE PROGRAM VISIBILITY
// =========================================================

    @Test
    void anonymousCanViewAnnouncedProgramById() {

        Program program = createProgram(
                10L,
                ProgramState.ANNOUNCED
        );

        when(programRepository.findById(10L))
                .thenReturn(Optional.of(program));

        Program result =
                programService.getVisibleProgramById(
                        10L,
                        null
                );

        assertSame(program, result);
    }


    @Test
    void anonymousCannotViewPrivateProgramById() {

        Program program = createProgram(
                10L,
                ProgramState.REVIEW
        );

        when(programRepository.findById(10L))
                .thenReturn(Optional.of(program));

        assertThrows(
                ForbiddenOperationException.class,
                () -> programService
                        .getVisibleProgramById(
                                10L,
                                null
                        )
        );
    }


    @Test
    void userWithProgramRoleCanViewPrivateProgramById() {

        User user = createUser(
                1L,
                "programmer"
        );

        Program program = createProgram(
                10L,
                ProgramState.REVIEW
        );

        ProgramRole role = new ProgramRole(
                user,
                program,
                ProgramRoleType.PROGRAMMER
        );

        when(programRepository.findById(10L))
                .thenReturn(Optional.of(program));

        when(programRoleRepository
                .findByUserAndProgram(
                        user,
                        program
                ))
                .thenReturn(Optional.of(role));

        Program result =
                programService.getVisibleProgramById(
                        10L,
                        user
                );

        assertSame(program, result);
    }


    @Test
    void userWithoutProgramRoleCannotViewPrivateProgramById() {

        User user = createUser(
                1L,
                "normaluser"
        );

        Program program = createProgram(
                10L,
                ProgramState.REVIEW
        );

        when(programRepository.findById(10L))
                .thenReturn(Optional.of(program));

        when(programRoleRepository
                .findByUserAndProgram(
                        user,
                        program
                ))
                .thenReturn(Optional.empty());

        assertThrows(
                ForbiddenOperationException.class,
                () -> programService
                        .getVisibleProgramById(
                                10L,
                                user
                        )
        );
    }

}