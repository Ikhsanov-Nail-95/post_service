package faang.school.postservice.exception;

public class UserServiceUnavailableException extends RuntimeException {
    public UserServiceUnavailableException(String message){
        super(message);
    }
}