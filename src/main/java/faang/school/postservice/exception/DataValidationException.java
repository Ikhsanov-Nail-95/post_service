package faang.school.postservice.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class DataValidationException extends RuntimeException{
    private final Map<String,String> details;

    public DataValidationException(String message) {
        super(message);
        this.details = null;
    }

    public DataValidationException(String message, Map<String,String> details) {
        super(message);
        this.details = details;
    }

}