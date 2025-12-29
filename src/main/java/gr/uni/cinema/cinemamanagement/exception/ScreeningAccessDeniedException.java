package gr.uni.cinema.cinemamanagement.exception;

public class ScreeningAccessDeniedException extends RuntimeException {
    public ScreeningAccessDeniedException(String message) {
        super(message);
    }
}
