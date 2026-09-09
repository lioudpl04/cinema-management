package gr.uni.cinema.cinemamanagement.dto;

import gr.uni.cinema.cinemamanagement.entity.ScreeningState;

import java.time.LocalDateTime;

public record ScreeningResponse(
        Long id,
        String movieTitle,
        String cast,
        String genres,
        Integer durationMinutes,
        ScreeningState state,

        Long programId,
        String programName,

        String submitterUsername,
        String assignedStaffUsername,

        String reviewComments,
        Integer reviewScore,
        LocalDateTime reviewedAt,
        String rejectionReason,
        String approvalNotes,

        boolean finalSubmitted,
        LocalDateTime finalSubmittedAt,

        String auditorium,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}