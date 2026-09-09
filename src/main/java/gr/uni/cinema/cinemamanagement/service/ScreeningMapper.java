package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.dto.ScreeningResponse;
import gr.uni.cinema.cinemamanagement.entity.Screening;

public class ScreeningMapper {

    private ScreeningMapper() {
    }

    // Metatrepei ena Screening entity se ScreeningResponse.
    public static ScreeningResponse toResponse(
            Screening screening) {

        String assignedStaffUsername = null;

        if (screening.getAssignedStaff() != null) {
            assignedStaffUsername =
                    screening.getAssignedStaff().getUsername();
        }

        return new ScreeningResponse(
                screening.getId(),
                screening.getMovieTitle(),
                screening.getCast(),
                screening.getGenres(),
                screening.getDurationMinutes(),
                screening.getState(),

                screening.getProgram().getId(),
                screening.getProgram().getName(),

                screening.getSubmitter().getUsername(),
                assignedStaffUsername,

                screening.getReviewComments(),
                screening.getReviewScore(),
                screening.getReviewedAt(),
                screening.getRejectionReason(),
                screening.getApprovalNotes(),

                screening.isFinalSubmitted(),
                screening.getFinalSubmittedAt(),

                screening.getAuditorium(),
                screening.getStartTime(),
                screening.getEndTime()
        );
    }
}