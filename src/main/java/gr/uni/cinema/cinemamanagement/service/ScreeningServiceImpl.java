package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.entity.*;
import gr.uni.cinema.cinemamanagement.exception.InvalidScreeningStateException;
import gr.uni.cinema.cinemamanagement.exception.ScreeningAccessDeniedException;
import gr.uni.cinema.cinemamanagement.repository.ProgramRepository;
import gr.uni.cinema.cinemamanagement.repository.ScreeningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ScreeningServiceImpl implements ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final ProgramRepository programRepository;

    @Override
    public Screening createScreening(String movieTitle, Long programId, User submitter) {

        // 🔐 ROLE CHECK — only USER can submit
        if (submitter.getRole() != UserRole.USER) {
            throw new ScreeningAccessDeniedException(
                    "Only users can submit screenings"
            );
        }

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Program not found"));

        Screening screening = Screening.builder()
                .movieTitle(movieTitle)
                .program(program)
                .submitter(submitter)
                .state(ScreeningState.CREATED)
                .build();

        return screeningRepository.save(screening);
    }

    @Override
    public Screening changeState(Long screeningId, ScreeningState newState, User user) {

        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new RuntimeException("Screening not found"));

        ScreeningState currentState = screening.getState();

        // 1️⃣ STATE TRANSITION CHECK
        if (!ScreeningStateRules.canTransition(currentState, newState)) {
            throw new InvalidScreeningStateException(
                    "Cannot change state from " + currentState + " to " + newState
            );
        }

        // 2️⃣ ROLE CHECK
        if (!ScreeningRoleRules.canChangeState(
                user.getRole(),
                currentState,
                newState
        )) {
            throw new ScreeningAccessDeniedException(
                    "Role " + user.getRole() +
                            " cannot change screening from " +
                            currentState + " to " + newState
            );
        }

        screening.setState(newState);
        return screeningRepository.save(screening);
    }

    @Override
    public Screening schedule(
            Long screeningId,
            String auditorium,
            LocalDateTime startTime,
            LocalDateTime endTime,
            User user
    ) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new RuntimeException("Screening not found"));

        // 1️⃣ must be APPROVED
        if (screening.getState() != ScreeningState.APPROVED) {
            throw new InvalidScreeningStateException(
                    "Only APPROVED screenings can be scheduled"
            );
        }

        // 2️⃣ role check — STAFF (ή PROGRAMMER αν αυτό χρησιμοποιείς)
        if (user.getRole() != UserRole.PROGRAMMER) {
            throw new ScreeningAccessDeniedException(
                    "Only staff can schedule screenings"
            );
        }

        // 3️⃣ set data
        screening.setAuditorium(auditorium);
        screening.setStartTime(startTime);
        screening.setEndTime(endTime);
        screening.setState(ScreeningState.SCHEDULED);

        return screeningRepository.save(screening);
    }
}
