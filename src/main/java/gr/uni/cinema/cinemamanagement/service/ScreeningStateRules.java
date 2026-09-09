package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.entity.ScreeningState;

import java.util.Set;

/**
 * Orizei tis epitrepomenes metavaseis metaxy ton ScreeningState.
 *
 * Vasiki epitiximeni roi:
 * CREATED
 * -> SUBMITTED
 * -> REVIEWED
 * -> APPROVED
 * -> SCHEDULED
 *
 * Ena REVIEWED i APPROVED screening mporei episis
 * na kataliksei se REJECTED.
 */
public class ScreeningStateRules {

    /**
     * Elegxei an mia metavasi apo ena screening state
     * se ena allo state epitrepetai.
     *
     * @param from trexon state tou screening
     * @param to neo state tou screening
     * @return true an i metavasi epitrepetai, diaforetika false
     */
    public static boolean canTransition(
            ScreeningState from,
            ScreeningState to) {

        return switch (from) {

            // Meta ti dimiourgia, to screening mporei mono na ginei submit.
            case CREATED ->
                    Set.of(
                            ScreeningState.SUBMITTED
                    ).contains(to);

            // Ena submitted screening mporei na proxorisei se reviewed.
            case SUBMITTED ->
                    Set.of(
                            ScreeningState.REVIEWED
                    ).contains(to);

            // Meta to review, to screening mporei na egkrithei i na aporrifthei.
            case REVIEWED ->
                    Set.of(
                            ScreeningState.APPROVED,
                            ScreeningState.REJECTED
                    ).contains(to);

            // Ena approved screening mporei na ginei scheduled i na aporrifthei.
            case APPROVED ->
                    Set.of(
                            ScreeningState.REJECTED,
                            ScreeningState.SCHEDULED
                    ).contains(to);

            // Ta SCHEDULED kai REJECTED einai telikes katastaseis.
            case SCHEDULED, REJECTED ->
                    false;
        };
    }
}