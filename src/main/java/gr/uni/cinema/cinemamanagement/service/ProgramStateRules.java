package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.entity.ProgramState;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Orizei tis epitrepomenes metavaseis metaxy ton ProgramState.
 *
 * Kathe program mporei na proxorisei mono stin epomeni
 * egkiri katastasi tou workflow.
 *
 * Kanoniki roi:
 * CREATED
 * -> SUBMISSION
 * -> ASSIGNMENT
 * -> REVIEW
 * -> SCHEDULING
 * -> FINAL_SUBMISSION
 * -> DECISION
 * -> ANNOUNCED
 */
public class ProgramStateRules {

    // Map pou krata gia kathe state poia epomena states epitrepontai.
    private static final Map<ProgramState, Set<ProgramState>> ALLOWED_TRANSITIONS =
            new EnumMap<>(ProgramState.class);

    static {
        ALLOWED_TRANSITIONS.put(
                ProgramState.CREATED,
                EnumSet.of(ProgramState.SUBMISSION)
        );

        ALLOWED_TRANSITIONS.put(
                ProgramState.SUBMISSION,
                EnumSet.of(ProgramState.ASSIGNMENT)
        );

        ALLOWED_TRANSITIONS.put(
                ProgramState.ASSIGNMENT,
                EnumSet.of(ProgramState.REVIEW)
        );

        ALLOWED_TRANSITIONS.put(
                ProgramState.REVIEW,
                EnumSet.of(ProgramState.SCHEDULING)
        );

        ALLOWED_TRANSITIONS.put(
                ProgramState.SCHEDULING,
                EnumSet.of(ProgramState.FINAL_SUBMISSION)
        );

        ALLOWED_TRANSITIONS.put(
                ProgramState.FINAL_SUBMISSION,
                EnumSet.of(ProgramState.DECISION)
        );

        ALLOWED_TRANSITIONS.put(
                ProgramState.DECISION,
                EnumSet.of(ProgramState.ANNOUNCED)
        );

        // To ANNOUNCED den exei epomeni katastasi,
        // giati einai to teliko state tou program.
    }

    public static boolean canTransition(ProgramState from, ProgramState to) {
        return ALLOWED_TRANSITIONS
                .getOrDefault(from, Set.of())
                .contains(to);
    }
}