package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.entity.ScreeningState;

import java.util.Set;

public class ScreeningStateRules {

    public static boolean canTransition(ScreeningState from, ScreeningState to) {

        return switch (from) {

            case CREATED ->
                    Set.of(ScreeningState.SUBMITTED).contains(to);

            case SUBMITTED ->
                    Set.of(
                            ScreeningState.REVIEWED,
                            ScreeningState.REJECTED
                    ).contains(to);

            case REVIEWED ->
                    Set.of(
                            ScreeningState.APPROVED,
                            ScreeningState.REJECTED
                    ).contains(to);

            case APPROVED ->
                    Set.of(ScreeningState.SCHEDULED).contains(to);

            case SCHEDULED, REJECTED ->
                    false;
        };
    }
}
