package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.entity.Screening;
import gr.uni.cinema.cinemamanagement.entity.ScreeningState;
import gr.uni.cinema.cinemamanagement.entity.User;

import java.time.LocalDateTime;

public interface ScreeningService {

    Screening createScreening(
            String movieTitle,
            Long programId,
            User submitter
    );

    Screening changeState(
            Long screeningId,
            ScreeningState newState,
            User user
    );

    Screening schedule(
            Long screeningId,
            String auditorium,
            LocalDateTime startTime,
            LocalDateTime endTime,
            User user
    );
}
