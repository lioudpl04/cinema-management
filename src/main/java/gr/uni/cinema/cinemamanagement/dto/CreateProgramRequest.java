package gr.uni.cinema.cinemamanagement.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateProgramRequest {

    // Vasika stoixeia gia ti dimiourgia program.
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}