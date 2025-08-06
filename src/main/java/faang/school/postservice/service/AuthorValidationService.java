package faang.school.postservice.service;

import faang.school.postservice.client.ProjectServiceClient;
import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.exception.UserServiceUnavailableException;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthorValidationService {

    private final UserServiceClient userServiceClient;
    private final ProjectServiceClient projectServiceClient;
    private final UserContext userContext;

    @Retryable(
            retryFor = UserServiceUnavailableException.class,
            noRetryFor = EntityNotFoundException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void validateUserExists(long userId) {
        try {
            userServiceClient.getUser(userId);
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("User not found.");
        } catch (FeignException e) {
            log.error("UserService unavailable while trying to validate userId = {}. Feign status: {}, message: {}",
                    userId, e.status(), e.getMessage(), e);

            throw new UserServiceUnavailableException("Service temporarily unavailable. Try again later.");
        }
    }

    @Retryable(
            retryFor = UserServiceUnavailableException.class,
            noRetryFor = EntityNotFoundException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void validateProjectExists(long projectId) {
        try {
            projectServiceClient.getProject(projectId, userContext.getUserId());
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("Project not found.");
        } catch (FeignException e) {
            log.error("ProjectService unavailable while trying to validate projectId {}. Feign status: {}, message: {}",
                    projectId, e.status(), e.getMessage(), e);

            throw new UserServiceUnavailableException("Service temporarily unavailable. Try again later.");
        }
    }

}