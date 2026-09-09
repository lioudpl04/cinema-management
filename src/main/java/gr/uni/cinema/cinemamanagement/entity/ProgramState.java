package gr.uni.cinema.cinemamanagement.entity;

/**
 * Katastaseis tou lifecycle enos program.
 *
 * Kanoniki roi:
 *
 * CREATED
 * -> SUBMISSION
 * -> ASSIGNMENT
 * -> REVIEW
 * -> SCHEDULING
 * -> FINAL_SUBMISSION
 * -> DECISION
 * -> ANNOUNCED
 */
public enum ProgramState {

    // To program exei dimiourgithei kai mporei akoma na rythmistei.
    CREATED,

    // Oi submitters mporoun na dimiourgoun kai na kanoun submit screenings.
    SUBMISSION,

    // O programmer anathetei ta submitted screenings se STAFF.
    ASSIGNMENT,

    // O assigned STAFF kanei review ta screenings.
    REVIEW,

    // Ta reviewed screenings mporoun na egkrithoun i na aporrifthoun.
    SCHEDULING,

    // Ta approved screenings mporoun na kanoun final submission.
    FINAL_SUBMISSION,

    // Ginontai oi telikes apofaseis gia ta screenings.
    DECISION,

    // Teliki katastasi tou program. Ta stoixeia einai pleon public.
    ANNOUNCED
}