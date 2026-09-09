package gr.uni.cinema.cinemamanagement.exception;

public class InvalidUserDataException extends RuntimeException {

    public InvalidUserDataException(String message) {
        super(message);
    }
}