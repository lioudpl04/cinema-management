package gr.uni.cinema.cinemamanagement.service;

import gr.uni.cinema.cinemamanagement.entity.ProgramState;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class ProgramStateRules {

    private static final Map<ProgramState, Set<ProgramState>> ALLOWED_TRANSITIONS =
            new EnumMap<>(ProgramState.class);

    static {
        ALLOWED_TRANSITIONS.put(ProgramState.CREATED,
                EnumSet.of(ProgramState.SUBMISSION));

        ALLOWED_TRANSITIONS.put(ProgramState.SUBMISSION,
                EnumSet.of(ProgramState.ASSIGNMENT));

        ALLOWED_TRANSITIONS.put(ProgramState.ASSIGNMENT,
                EnumSet.of(ProgramState.REVIEW));

        ALLOWED_TRANSITIONS.put(ProgramState.REVIEW,
                EnumSet.of(ProgramState.SCHEDULING));

        ALLOWED_TRANSITIONS.put(ProgramState.SCHEDULING,
                EnumSet.of(ProgramState.FINAL_PUBLICATION));

        ALLOWED_TRANSITIONS.put(ProgramState.FINAL_PUBLICATION,
                EnumSet.of(ProgramState.DECISION));

        ALLOWED_TRANSITIONS.put(ProgramState.DECISION,
                EnumSet.of(ProgramState.ANNOUNCED));
    }

    public static boolean canTransition(ProgramState from, ProgramState to) {
        return ALLOWED_TRANSITIONS
                .getOrDefault(from, Set.of())
                .contains(to);
    }
}
