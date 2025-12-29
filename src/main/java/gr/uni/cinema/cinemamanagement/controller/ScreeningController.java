package gr.uni.cinema.cinemamanagement.controller;

import gr.uni.cinema.cinemamanagement.dto.CreateScreeningRequest;
import gr.uni.cinema.cinemamanagement.dto.ScheduleScreeningRequest;
import gr.uni.cinema.cinemamanagement.entity.Screening;
import gr.uni.cinema.cinemamanagement.entity.ScreeningState;
import gr.uni.cinema.cinemamanagement.entity.User;
import gr.uni.cinema.cinemamanagement.repository.UserRepository;
import gr.uni.cinema.cinemamanagement.service.ScreeningService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/screenings")
public class ScreeningController {

    private final ScreeningService screeningService;
    private final UserRepository userRepository;

    public ScreeningController(
            ScreeningService screeningService,
            UserRepository userRepository
    ) {
        this.screeningService = screeningService;
        this.userRepository = userRepository;
    }

    // 👤 USER (submit screening)
    private User mockUser() {
        return userRepository.findByUsername("user1")
                .orElseThrow(() -> new RuntimeException("Mock user not found"));
    }

    // 👨‍💼 STAFF / PROGRAMMER (review, approve, schedule)
    private User mockStaff() {
        return userRepository.findByUsername("programmer1")
                .orElseThrow(() -> new RuntimeException("Mock staff not found"));
    }

    // CREATE screening (USER)
    @PostMapping
    public Screening createScreening(
            @RequestBody CreateScreeningRequest request
    ) {
        return screeningService.createScreening(
                request.getMovieTitle(),
                request.getProgramId(),
                mockUser()
        );
    }

    // CHANGE state (STAFF)
    @PatchMapping("/{id}/state")
    public Screening changeState(
            @PathVariable Long id,
            @RequestParam ScreeningState newState
    ) {
        return screeningService.changeState(
                id,
                newState,
                mockStaff()
        );
    }

    // SCHEDULE screening (STAFF)
    @PatchMapping("/{id}/schedule")
    public Screening schedule(
            @PathVariable Long id,
            @RequestBody ScheduleScreeningRequest request
    ) {
        return screeningService.schedule(
                id,
                request.auditorium(),
                request.startTime(),
                request.endTime(),
                mockStaff()
        );
    }
}
