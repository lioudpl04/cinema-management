package gr.uni.cinema.cinemamanagement.exception;

public class ProgramNameAlreadyExistsException extends RuntimeException {

    public ProgramNameAlreadyExistsException(String message) {
        super(message);
    }
}