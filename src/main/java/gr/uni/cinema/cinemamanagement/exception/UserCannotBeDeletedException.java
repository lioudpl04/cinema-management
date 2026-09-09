package gr.uni.cinema.cinemamanagement.exception;

public class UserCannotBeDeletedException extends RuntimeException {

    public UserCannotBeDeletedException(String message) {
        super(message);
    }
}