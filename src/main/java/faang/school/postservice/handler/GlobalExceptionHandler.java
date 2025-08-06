package faang.school.postservice.handler;

import faang.school.postservice.dto.response.ErrorResponse;
import faang.school.postservice.exception.DataValidationException;
import faang.school.postservice.exception.EventPublishException;
import faang.school.postservice.exception.JsonSerializationException;
import faang.school.postservice.exception.UserServiceUnavailableException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String EXCEPTION = "exception";
    private static final String PATH = "path";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            errors.put("Data", getCurrentTimestamp());
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                null,
                getCurrentTimestamp()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(DataValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(DataValidationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                ex.getDetails(),
                getCurrentTimestamp()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(JsonSerializationException.class)
    public ProblemDetail handleJsonSerializationException(JsonSerializationException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        problem.setTitle("JSON Serialization Error");
        problem.setDetail(ex.getMessage());
        problem.setProperty(EXCEPTION, ex.getClass().getSimpleName());
        problem.setProperty(PATH, request.getRequestURI());

        return problem;
    }

    @ExceptionHandler(EventPublishException.class)
    public ProblemDetail handleEventPublishException(EventPublishException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);

        problem.setTitle("Redis Publish Error");
        problem.setDetail(ex.getMessage());
        problem.setProperty(EXCEPTION, ex.getClass().getSimpleName());
        problem.setInstance(URI.create("/api/v1/errors/redis-publish"));
        problem.setProperty(PATH, request.getRequestURI());

        return problem;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problem.setTitle("Entity Not Found");
        problem.setDetail(ex.getMessage());
        problem.setProperty(EXCEPTION, ex.getClass().getSimpleName());
        problem.setInstance(URI.create("/api/v1/errors/entity-not-found"));
        problem.setProperty(PATH, request.getRequestURI());

        return problem;
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    public ProblemDetail handleServiceUnavailableException(UserServiceUnavailableException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);

        problem.setTitle("Service Unavailable");
        problem.setDetail(ex.getMessage());
        problem.setProperty(EXCEPTION, ex.getClass().getSimpleName());
        problem.setInstance(URI.create("/api/v1/errors/service-unavailable"));
        problem.setProperty(PATH, request.getRequestURI());

        return problem;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleBadRequest(HttpMessageNotReadableException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Malformed JSON request");
        problem.setDetail(ex.getMessage());
        problem.setProperty(EXCEPTION, ex.getClass().getSimpleName());
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        problem.setTitle("Internal Server Error");
        problem.setDetail(ex.getMessage());
        problem.setProperty(EXCEPTION, ex.getClass().getSimpleName());
        problem.setProperty(PATH, request.getRequestURI());

        return problem;
    }

    private String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX'['VV']'");
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        return zonedDateTime.format(formatter);
    }

}