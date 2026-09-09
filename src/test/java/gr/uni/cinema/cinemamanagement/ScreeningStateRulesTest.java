package gr.uni.cinema.cinemamanagement;

import gr.uni.cinema.cinemamanagement.entity.ScreeningState;
import gr.uni.cinema.cinemamanagement.service.ScreeningStateRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ScreeningStateRulesTest {

    @Test
    void createdCanMoveToSubmitted() {
        assertTrue(
                ScreeningStateRules.canTransition(
                        ScreeningState.CREATED,
                        ScreeningState.SUBMITTED
                )
        );
    }

    @Test
    void createdCannotMoveDirectlyToReviewed() {
        assertFalse(
                ScreeningStateRules.canTransition(
                        ScreeningState.CREATED,
                        ScreeningState.REVIEWED
                )
        );
    }

    @Test
    void submittedCanMoveToReviewed() {
        assertTrue(
                ScreeningStateRules.canTransition(
                        ScreeningState.SUBMITTED,
                        ScreeningState.REVIEWED
                )
        );
    }

    @Test
    void submittedCannotMoveDirectlyToRejected() {
        assertFalse(
                ScreeningStateRules.canTransition(
                        ScreeningState.SUBMITTED,
                        ScreeningState.REJECTED
                )
        );
    }

    @Test
    void reviewedCanMoveToApproved() {
        assertTrue(
                ScreeningStateRules.canTransition(
                        ScreeningState.REVIEWED,
                        ScreeningState.APPROVED
                )
        );
    }

    @Test
    void reviewedCanMoveToRejected() {
        assertTrue(
                ScreeningStateRules.canTransition(
                        ScreeningState.REVIEWED,
                        ScreeningState.REJECTED
                )
        );
    }

    @Test
    void approvedCanMoveToScheduled() {
        assertTrue(
                ScreeningStateRules.canTransition(
                        ScreeningState.APPROVED,
                        ScreeningState.SCHEDULED
                )
        );
    }

    @Test
    void approvedCannotMoveBackToReviewed() {
        assertFalse(
                ScreeningStateRules.canTransition(
                        ScreeningState.APPROVED,
                        ScreeningState.REVIEWED
                )
        );
    }

    @Test
    void scheduledIsFinalState() {
        assertFalse(
                ScreeningStateRules.canTransition(
                        ScreeningState.SCHEDULED,
                        ScreeningState.REJECTED
                )
        );
    }

    @Test
    void rejectedIsFinalState() {
        assertFalse(
                ScreeningStateRules.canTransition(
                        ScreeningState.REJECTED,
                        ScreeningState.CREATED
                )
        );
    }

    @Test
    void approvedCanMoveToRejected() {
        assertTrue(
                ScreeningStateRules.canTransition(
                        ScreeningState.APPROVED,
                        ScreeningState.REJECTED
                )
        );
    }



}