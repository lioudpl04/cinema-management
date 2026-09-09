package gr.uni.cinema.cinemamanagement.exception;

public class ProgramNotFoundException extends RuntimeException {

    public ProgramNotFoundException(String message) {
        super(message);
    }
}