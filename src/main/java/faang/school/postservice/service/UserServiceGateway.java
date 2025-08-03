package faang.school.postservice.service;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.client.dto.UserDto;
import faang.school.postservice.exception.UserServiceUnavailableException;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceGateway {
    private final UserServiceClient userServiceClient;

    @Retryable(
            retryFor = UserServiceUnavailableException.class,
            noRetryFor = EntityNotFoundException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public UserDto getUser(long userId) {
        try {
            return userServiceClient.getUser(userId);
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("User not found.");
        } catch (FeignException e) {
            log.error("UserService unavailable while fetching userId = {}: {}, {}",
                    userId, e.status(), e.getMessage(), e);
            throw new UserServiceUnavailableException("Service temporarily unavailable. Try again later.");
        }
    }

    @Retryable(
            retryFor = UserServiceUnavailableException.class,
            noRetryFor = EntityNotFoundException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public List<UserDto> getUsersByIds(List<Long> userIds) {
        try {
            return userServiceClient.getUsersByIds(userIds);
        } catch (FeignException e) {
            log.error("UserService unavailable while fetching user list. Feign status: {}, message: {}", e.status(), e.getMessage(), e);
            throw new UserServiceUnavailableException("Service temporarily unavailable. Try again later.");
        }
    }
}