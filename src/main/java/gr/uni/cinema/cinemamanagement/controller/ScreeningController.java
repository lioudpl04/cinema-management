package gr.uni.cinema.cinemamanagement.controller;

import gr.uni.cinema.cinemamanagement.dto.*;
import gr.uni.cinema.cinemamanagement.entity.Screening;
import gr.uni.cinema.cinemamanagement.entity.ScreeningState;
import gr.uni.cinema.cinemamanagement.entity.User;
import gr.uni.cinema.cinemamanagement.exception.UserNotFoundException;
import gr.uni.cinema.cinemamanagement.repository.UserRepository;
import gr.uni.cinema.cinemamanagement.service.ScreeningMapper;
import gr.uni.cinema.cinemamanagement.service.ScreeningService;
import gr.uni.cinema.cinemamanagement.exception.InvalidCredentialsException;
import gr.uni.cinema.cinemamanagement.service.UserService;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/screenings")
public class ScreeningController {

    private final ScreeningService screeningService;
    private final UserRepository userRepository;
    private final UserService userService;

    public ScreeningController(
            ScreeningService screeningService,
            UserRepository userRepository,
            UserService userService) {

        this.screeningService = screeningService;
        this.userRepository = userRepository;
        this.userService = userService;
    }


    // Dimiourgia screening apo SUBMITTER.
    @PostMapping
    public ScreeningResponse createScreening(
            @RequestBody CreateScreeningRequest request,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        Screening screening =
                screeningService.createScreening(
                        request.getMovieTitle(),
                        request.getCast(),
                        request.getGenres(),
                        request.getDurationMinutes(),
                        request.getProgramId(),
                        currentUser
                );

        return ScreeningMapper.toResponse(screening);
    }


    // Update screening apo ton SUBMITTER.
    @PatchMapping("/{id}")
    public ScreeningResponse updateScreening(
            @PathVariable Long id,
            @RequestBody UpdateScreeningRequest request,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        Screening screening =
                screeningService.updateScreening(
                        id,
                        request.movieTitle(),
                        request.cast(),
                        request.genres(),
                        request.durationMinutes(),
                        request.auditorium(),
                        request.startTime(),
                        currentUser
                );

        return ScreeningMapper.toResponse(screening);
    }


    // Submit screening: CREATED -> SUBMITTED.
    @PatchMapping("/{id}/submit")
    public ScreeningResponse submitScreening(
            @PathVariable Long id,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        Screening screening =
                screeningService.changeState(
                        id,
                        ScreeningState.SUBMITTED,
                        currentUser
                );

        return ScreeningMapper.toResponse(screening);
    }


    // Aposyrsi kai diagrafi CREATED screening apo ton SUBMITTER.
    @DeleteMapping("/{id}/withdraw")
    public void withdrawScreening(
            @PathVariable Long id,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        screeningService.withdraw(
                id,
                currentUser
        );
    }


    // Anathesi STAFF apo PROGRAMMER.
    @PatchMapping("/{id}/assign/{staffId}")
    public ScreeningResponse assignStaff(
            @PathVariable Long id,
            @PathVariable Long staffId,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        User staff = userRepository.findById(staffId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "Staff user not found"
                        )
                );

        Screening screening =
                screeningService.assignStaff(
                        id,
                        staff,
                        currentUser
                );

        return ScreeningMapper.toResponse(screening);
    }


    // Review apo ton assigned STAFF.
    @PatchMapping("/{id}/review")
    public ScreeningResponse reviewScreening(
            @PathVariable Long id,
            @RequestBody ReviewScreeningRequest request,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        Screening screening =
                screeningService.review(
                        id,
                        request.score(),
                        request.comments(),
                        currentUser
                );

        return ScreeningMapper.toResponse(screening);
    }


    // Egkrisi screening: REVIEWED -> APPROVED.
    @PatchMapping("/{id}/approve")
    public ScreeningResponse approveScreening(
            @PathVariable Long id,
            @RequestBody ApproveScreeningRequest request,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        Screening screening =
                screeningService.approve(
                        id,
                        request.approvalNotes(),
                        currentUser
                );

        return ScreeningMapper.toResponse(screening);
    }


    // Aporripsi screening.
    @PatchMapping("/{id}/reject")
    public ScreeningResponse rejectScreening(
            @PathVariable Long id,
            @RequestBody RejectScreeningRequest request,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        Screening screening =
                screeningService.reject(
                        id,
                        request.reason(),
                        currentUser
                );

        return ScreeningMapper.toResponse(screening);
    }


    // Teliko scheduling apo PROGRAMMER.
    @PatchMapping("/{id}/schedule")
    public ScreeningResponse schedule(
            @PathVariable Long id,
            @RequestBody ScheduleScreeningRequest request,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        Screening screening =
                screeningService.schedule(
                        id,
                        request.auditorium(),
                        request.startTime(),
                        request.endTime(),
                        currentUser
                );

        return ScreeningMapper.toResponse(screening);
    }


    // Epistrofi ton screenings pou mporei na dei o user.
    @GetMapping
    public List<ScreeningResponse> getVisibleScreenings(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        return screeningService
                .getVisibleScreenings(currentUser)
                .stream()
                .map(ScreeningMapper::toResponse)
                .toList();
    }


    // Anazitisi sta visible screenings.
    @GetMapping("/search")
    public List<ScreeningResponse> searchScreenings(
            @RequestParam(required = false) String movieTitle,
            @RequestParam(required = false) String cast,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        return screeningService.searchScreenings(
                        movieTitle,
                        cast,
                        genre,
                        fromDate,
                        toDate,
                        currentUser
                )
                .stream()
                .map(ScreeningMapper::toResponse)
                .toList();
    }


    // Public screenings xoris authentication.
    @GetMapping("/public")
    public List<PublicScreeningResponse> getPublicScreenings() {

        return screeningService
                .getPublicScreenings()
                .stream()
                .map(screening -> new PublicScreeningResponse(
                        screening.getId(),
                        screening.getMovieTitle(),
                        screening.getCast(),
                        screening.getGenres(),
                        screening.getDurationMinutes(),
                        screening.getProgram().getId(),
                        screening.getProgram().getName(),
                        screening.getAuditorium(),
                        screening.getStartTime(),
                        screening.getEndTime()
                ))
                .toList();
    }


    // Epistrofi enos screening me elegxo visibility.
    @GetMapping("/{id}")
    public ScreeningResponse getScreeningById(
            @PathVariable Long id,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        Screening screening =
                screeningService.getVisibleScreeningById(
                        id,
                        currentUser
                );

        return ScreeningMapper.toResponse(screening);
    }


    // Final submission apo ton SUBMITTER.
    @PatchMapping("/{id}/final-submit")
    public ScreeningResponse finalSubmit(
            @PathVariable Long id,
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader) {

        User currentUser =
                getUserFromAuthorizationHeader(
                        authorizationHeader
                );

        Screening screening =
                screeningService.finalSubmit(
                        id,
                        currentUser
                );

        return ScreeningMapper.toResponse(screening);
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