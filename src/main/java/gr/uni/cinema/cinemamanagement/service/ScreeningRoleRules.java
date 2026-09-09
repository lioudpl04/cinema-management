package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.entity.ProgramRoleType;
import gr.uni.cinema.cinemamanagement.entity.ScreeningState;

import java.util.Set;

public class ScreeningRoleRules {

    // Elegxei an o rolos mporei na kanei ti sygkekrimeni allagi state.
    public static boolean canChangeState(
            ProgramRoleType role,
            ScreeningState from,
            ScreeningState to) {

        return switch (from) {

            // O SUBMITTER kanei submit to screening.
            case CREATED ->
                    role == ProgramRoleType.SUBMITTER
                            && to == ScreeningState.SUBMITTED;

            // O STAFF kanei review to screening.
            case SUBMITTED ->
                    role == ProgramRoleType.STAFF
                            && to == ScreeningState.REVIEWED;

            // O SUBMITTER mporei na egkrinei i na aporripsei meta to review.
            case REVIEWED ->
                    role == ProgramRoleType.SUBMITTER
                            && Set.of(
                            ScreeningState.APPROVED,
                            ScreeningState.REJECTED
                    ).contains(to);

            // O PROGRAMMER kanei to teliko scheduling i rejection.
            case APPROVED ->
                    role == ProgramRoleType.PROGRAMMER
                            && Set.of(
                            ScreeningState.SCHEDULED,
                            ScreeningState.REJECTED
                    ).contains(to);

            // Telikes katastaseis.
            case SCHEDULED, REJECTED -> false;
        };
    }
}