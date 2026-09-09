package gr.uni.cinema.cinemamanagement.exception;

public class ScreeningNotFoundException extends RuntimeException {

    public ScreeningNotFoundException(String message) {
        super(message);
    }
}