package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.entity.ScreeningState;
import gr.uni.cinema.cinemamanagement.entity.UserRole;

import java.util.Set;

public class ScreeningRoleRules {

    public static boolean canChangeState(
            UserRole role,
            ScreeningState from,
            ScreeningState to
    ) {
        return switch (from) {

            // 👤 USER submits screening
            case CREATED ->
                    role == UserRole.USER
                            && to == ScreeningState.SUBMITTED;

            // 👨‍💼 PROGRAMMER reviews or rejects
            case SUBMITTED ->
                    role == UserRole.PROGRAMMER
                            && Set.of(
                            ScreeningState.REVIEWED,
                            ScreeningState.REJECTED
                    ).contains(to);

            // 👨‍💼 PROGRAMMER approves or rejects
            case REVIEWED ->
                    role == UserRole.PROGRAMMER
                            && Set.of(
                            ScreeningState.APPROVED,
                            ScreeningState.REJECTED
                    ).contains(to);

            // 👨‍💼 PROGRAMMER schedules
            case APPROVED ->
                    role == UserRole.PROGRAMMER
                            && to == ScreeningState.SCHEDULED;

            // ❌ terminal states
            case SCHEDULED, REJECTED ->
                    false;
        };
    }
}
