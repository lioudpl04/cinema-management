package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.entity.Screening;
import gr.uni.cinema.cinemamanagement.entity.ScreeningState;
import gr.uni.cinema.cinemamanagement.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ScreeningService {

    // Dimiourgia neou screening.
    Screening createScreening(
            String movieTitle,
            String cast,
            String genres,
            Integer durationMinutes,
            Long programId,
            User submitter
    );

    // Allagi state tou screening.
    Screening changeState(
            Long screeningId,
            ScreeningState newState,
            User user
    );

    // Anathesi STAFF se screening apo PROGRAMMER.
    Screening assignStaff(
            Long screeningId,
            User staff,
            User programmer
    );

    // Orismos auditorium kai oras provolis.
    Screening schedule(
            Long screeningId,
            String auditorium,
            LocalDateTime startTime,
            LocalDateTime endTime,
            User user
    );

    // Review tou screening apo ton assigned STAFF.
    Screening review(
            Long screeningId,
            Integer score,
            String comments,
            User staff
    );

    // Aporripsi screening.
    Screening reject(
            Long screeningId,
            String reason,
            User programmer
    );

    // Aposyrsi screening apo ton submitter.
    void withdraw(
            Long screeningId,
            User submitter
    );

    // Enimerosi ton stoixeion tou screening.
    Screening updateScreening(
            Long screeningId,
            String movieTitle,
            String cast,
            String genres,
            Integer durationMinutes,
            String auditorium,
            LocalDateTime startTime,
            User submitter
    );

    // Screenings pou mporei na dei o sygkekrimenos user.
    List<Screening> getVisibleScreenings(User user);

    // Anazitisi screenings me optional filters.
    List<Screening> searchScreenings(
            String movieTitle,
            String cast,
            String genre,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            User user
    );

    // Epistrefei screening mono an o user exei dikaioma na to dei.
    Screening getVisibleScreeningById(
            Long screeningId,
            User user
    );

    // Public screenings apo ANNOUNCED programs.
    List<Screening> getPublicScreenings();

    // Teliki ypovoli enos approved screening.
    Screening finalSubmit(
            Long screeningId,
            User submitter
    );

    // Egkrisi enos reviewed screening.
    Screening approve(
            Long screeningId,
            String approvalNotes,
            User submitter
    );
}