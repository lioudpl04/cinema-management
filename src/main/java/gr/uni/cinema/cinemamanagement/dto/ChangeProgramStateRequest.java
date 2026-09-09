package gr.uni.cinema.cinemamanagement.dto;

import gr.uni.cinema.cinemamanagement.entity.ProgramState;

public record ChangeProgramStateRequest(

        // Neo state pou ziteitai gia to program.
        ProgramState newState

) {
}