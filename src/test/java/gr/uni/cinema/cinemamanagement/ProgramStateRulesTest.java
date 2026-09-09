package gr.uni.cinema.cinemamanagement;

import gr.uni.cinema.cinemamanagement.entity.ProgramState;

import gr.uni.cinema.cinemamanagement.service.ProgramStateRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProgramStateRulesTest {

    @Test
    void createdCanMoveToSubmission() {

        boolean result = ProgramStateRules.canTransition(
                ProgramState.CREATED,
                ProgramState.SUBMISSION
        );

        assertTrue(result);
    }

    @Test
    void createdCannotMoveDirectlyToReview() {

        boolean result = ProgramStateRules.canTransition(
                ProgramState.CREATED,
                ProgramState.REVIEW
        );

        assertFalse(result);
    }

    @Test
    void submissionCanMoveToAssignment() {
        assertTrue(
                ProgramStateRules.canTransition(
                        ProgramState.SUBMISSION,
                        ProgramState.ASSIGNMENT
                )
        );
    }

    @Test
    void assignmentCanMoveToReview() {
        assertTrue(
                ProgramStateRules.canTransition(
                        ProgramState.ASSIGNMENT,
                        ProgramState.REVIEW
                )
        );
    }

    @Test
    void reviewCanMoveToScheduling() {
        assertTrue(
                ProgramStateRules.canTransition(
                        ProgramState.REVIEW,
                        ProgramState.SCHEDULING
                )
        );
    }

    @Test
    void schedulingCanMoveToFinalPublication() {
        assertTrue(
                ProgramStateRules.canTransition(
                        ProgramState.SCHEDULING,
                        ProgramState.FINAL_SUBMISSION
                )
        );
    }

    @Test
    void finalPublicationCanMoveToDecision() {
        assertTrue(
                ProgramStateRules.canTransition(
                        ProgramState.FINAL_SUBMISSION,
                        ProgramState.DECISION
                )
        );
    }

    @Test
    void decisionCanMoveToAnnounced() {
        assertTrue(
                ProgramStateRules.canTransition(
                        ProgramState.DECISION,
                        ProgramState.ANNOUNCED
                )
        );
    }
}