package gr.uni.cinema.cinemamanagement.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    // Lathos state tou program.
    @ExceptionHandler(InvalidProgramStateException.class)
    public ResponseEntity<?> handleInvalidState(
            InvalidProgramStateException ex) {

        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        LocalDateTime.now()
                )
        );
    }


    // Mi epitrepomeni energeia.
    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<?> handleForbidden(
            ForbiddenOperationException ex,
            HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", 403);
        body.put("error", "Forbidden");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(body);
    }


    // User den vrethike.
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(
            UserNotFoundException ex,
            HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", 404);
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }


    // To username yparxei idi.
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<?> handleUsernameAlreadyExists(
            UsernameAlreadyExistsException ex,
            HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", 409);
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }


    // Lathos state tou screening.
    @ExceptionHandler(InvalidScreeningStateException.class)
    public ResponseEntity<?> handleInvalidScreeningState(
            InvalidScreeningStateException ex,
            HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity
                .badRequest()
                .body(body);
    }


    // O user den exei prosvasi sto screening.
    @ExceptionHandler(ScreeningAccessDeniedException.class)
    public ResponseEntity<?> handleScreeningAccessDenied(
            ScreeningAccessDeniedException ex,
            HttpServletRequest request) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                ex.getMessage(),
                                LocalDateTime.now()
                        )
                );
    }


    // Lathos credentials i token.
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<?> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", 401);
        body.put("error", "Unauthorized");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }


    // Screening den vrethike.
    @ExceptionHandler(ScreeningNotFoundException.class)
    public ResponseEntity<?> handleScreeningNotFound(
            ScreeningNotFoundException ex,
            HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", 404);
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }


    // Program den vrethike.
    @ExceptionHandler(ProgramNotFoundException.class)
    public ResponseEntity<?> handleProgramNotFound(
            ProgramNotFoundException ex,
            HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", 404);
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }


    // To onoma tou program yparxei idi.
    @ExceptionHandler(ProgramNameAlreadyExistsException.class)
    public ResponseEntity<?> handleProgramNameAlreadyExists(
            ProgramNameAlreadyExistsException ex,
            HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", 409);
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }


    // Mi egkira stoixeia user.
    @ExceptionHandler(InvalidUserDataException.class)
    public ResponseEntity<?> handleInvalidUserData(
            InvalidUserDataException ex,
            HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }


    // O user den mporei na diagrafei.
    @ExceptionHandler(UserCannotBeDeletedException.class)
    public ResponseEntity<?> handleUserCannotBeDeleted(
            UserCannotBeDeletedException ex,
            HttpServletRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", 409);
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(body);
    }
}