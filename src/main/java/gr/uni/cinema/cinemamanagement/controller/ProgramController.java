package gr.uni.cinema.cinemamanagement.controller;

import gr.uni.cinema.cinemamanagement.dto.*;
import gr.uni.cinema.cinemamanagement.entity.Program;
import gr.uni.cinema.cinemamanagement.entity.ProgramRole;
import gr.uni.cinema.cinemamanagement.entity.ProgramRoleType;
import gr.uni.cinema.cinemamanagement.entity.User;
import gr.uni.cinema.cinemamanagement.exception.UserNotFoundException;
import gr.uni.cinema.cinemamanagement.repository.UserRepository;
import gr.uni.cinema.cinemamanagement.service.ProgramMapper;
import gr.uni.cinema.cinemamanagement.service.ProgramService;
import gr.uni.cinema.cinemamanagement.exception.InvalidCredentialsException;
import gr.uni.cinema.cinemamanagement.service.UserService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/programs")
public class ProgramController {

    private final ProgramService programService;
    private final UserRepository userRepository;
    private final UserService userService;

    public ProgramController(
            ProgramService programService,
            UserRepository userRepository,
            UserService userService) {

        this.programService = programService;
        this.userRepository = userRepository;
        this.userService = userService;
    }


    // Dimiourgia neou program.
    @PostMapping
    public ProgramResponse createProgram(
            @RequestBody CreateProgramRequest request,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        Program created = programService.createProgram(
                request.getName(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                currentUser
        );

        return ProgramMapper.toResponse(created);
    }


    // Epistrofi program me elegxo visibility.
    @GetMapping("/{id}")
    public ProgramResponse getProgram(
            @PathVariable Long id,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser = null;

        // To token einai optional gia public ANNOUNCED programs.
        if (authorizationHeader != null
                && !authorizationHeader.isBlank()) {

            currentUser =
                    getUserFromAuthorizationHeader(
                            authorizationHeader
                    );
        }

        Program program =
                programService.getVisibleProgramById(
                        id,
                        currentUser
                );

        return ProgramMapper.toResponse(program);
    }


    // Anazitisi programs me optional filters.
    @GetMapping
    public List<ProgramResponse> searchPrograms(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) java.time.LocalDate fromDate,
            @RequestParam(required = false) java.time.LocalDate toDate,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser = null;

        // Visitors mporoun na doun ta public ANNOUNCED programs.
        if (authorizationHeader != null
                && !authorizationHeader.isBlank()) {

            currentUser =
                    getUserFromAuthorizationHeader(
                            authorizationHeader
                    );
        }

        return programService.searchPrograms(
                        name,
                        fromDate,
                        toDate,
                        currentUser
                )
                .stream()
                .map(ProgramMapper::toResponse)
                .toList();
    }


    // Update program apo PROGRAMMER.
    @PatchMapping("/{id}")
    public ProgramResponse updateProgram(
            @PathVariable Long id,
            @RequestBody UpdateProgramRequest request,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        Program updated =
                programService.updateProgram(
                        id,
                        request.name(),
                        request.description(),
                        request.startDate(),
                        request.endDate(),
                        currentUser
                );

        return ProgramMapper.toResponse(updated);
    }


    // Allagi state tou program.
    @PatchMapping("/{id}/state")
    public ProgramResponse changeState(
            @PathVariable Long id,
            @RequestBody ChangeProgramStateRequest request,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        Program updated = programService.changeState(
                id,
                request.newState(),
                currentUser
        );

        return ProgramMapper.toResponse(updated);
    }


    // Diagrafi program apo PROGRAMMER oso einai CREATED.
    @DeleteMapping("/{id}")
    public void deleteProgram(
            @PathVariable Long id,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        programService.deleteProgram(
                id,
                currentUser
        );
    }


    // Prostheti PROGRAMMER sto program.
    @PostMapping("/{programId}/programmers/{userId}")
    public ProgramRoleResponse addProgrammer(
            @PathVariable Long programId,
            @PathVariable Long userId,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        ProgramRole programRole = programService.addRole(
                programId,
                targetUser,
                ProgramRoleType.PROGRAMMER,
                currentUser
        );

        return new ProgramRoleResponse(
                programRole.getId(),
                programRole.getUser().getId(),
                programRole.getUser().getUsername(),
                programRole.getProgram().getId(),
                programRole.getProgram().getName(),
                programRole.getRole()
        );
    }


    // Prostheti STAFF sto program.
    @PostMapping("/{programId}/staff/{userId}")
    public ProgramRoleResponse addStaff(
            @PathVariable Long programId,
            @PathVariable Long userId,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found")
                );

        ProgramRole programRole = programService.addRole(
                programId,
                targetUser,
                ProgramRoleType.STAFF,
                currentUser
        );

        return new ProgramRoleResponse(
                programRole.getId(),
                programRole.getUser().getId(),
                programRole.getUser().getUsername(),
                programRole.getProgram().getId(),
                programRole.getProgram().getName(),
                programRole.getRole()
        );
    }


    // Pairnei ton user apo to Bearer token.
    private User getUserFromAuthorizationHeader(
            String authorizationHeader) {

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            throw new InvalidCredentialsException(
                    "Authorization token is required"
            );
        }

        String token = authorizationHeader.substring(7);

        return userService.validateToken(token);
    }
}