package gr.uni.cinema.cinemamanagement.entity;

/**
 * Katastaseis tou lifecycle enos screening.
 *
 * Vasiki epitiximeni roi:
 *
 * CREATED
 * -> SUBMITTED
 * -> REVIEWED
 * -> APPROVED
 * -> SCHEDULED
 *
 * Ena screening mporei episis na kataliksei se REJECTED
 * otan aporrifthei kata ti diarkeia tis diadikasias.
 */
public enum ScreeningState {

    // To screening exei dimiourgithei apo ton submitter.
    CREATED,

    // To screening exei ginei submit kai perimenei tin epomeni fasi.
    SUBMITTED,

    // To screening exei axiologithei apo ton assigned STAFF.
    REVIEWED,

    // To screening exei egkrithei kai mporei na proxorisei pros teliki ypovoli.
    APPROVED,

    // To screening exei entaxthei sto teliko programma provolon.
    SCHEDULED,

    // To screening exei aporrifthei.
    REJECTED
}