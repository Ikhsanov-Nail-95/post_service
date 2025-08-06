package faang.school.postservice.service;

import faang.school.postservice.client.ProjectServiceClient;
import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.exception.UserServiceUnavailableException;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = "spring.liquibase.enabled=false")
class AuthorValidationServiceRetryTest {

    @MockBean private UserServiceClient userServiceClient;
    @MockBean private ProjectServiceClient projectServiceClient;
    @MockBean private UserContext userContext;

    @Autowired private AuthorValidationService authorValidationService;

    private final long userId = 1L;
    private final long projectId = 2L;
    private final  long currentUserId = 3L;

    @Test
    @DisplayName("Should retry 3 times and throw UserServiceUnavailableException when User Service fails")
    void validateUserExists_shouldRetry_whenUserServiceFails() {
        FeignException.InternalServerError feignException = mock(FeignException.InternalServerError.class);
        when(feignException.status()).thenReturn(503);
        when(feignException.getMessage()).thenReturn("Service temporarily unavailable. Try again later.");

        when(userServiceClient.getUser(userId)).thenThrow(feignException);

        assertThrows(UserServiceUnavailableException.class,
                () -> authorValidationService.validateUserExists(userId));

        verify(userServiceClient, times(3)).getUser(userId);
    }

    @Test
    @DisplayName("Should not retry and immediately throw EntityNotFoundException for 404")
    void validateUserExists_shouldNotRetry_whenUserNotFound() {
        when(userServiceClient.getUser(userId)).thenThrow(FeignException.NotFound.class);

        assertThrows(EntityNotFoundException.class,
                () -> authorValidationService.validateUserExists(userId));

        verify(userServiceClient, times(1)).getUser(userId);
    }

    @Test
    @DisplayName("Should retry 3 times and throw UserServiceUnavailableException when Project Service fails")
    void validateProjectExists_shouldRetry_whenProjectServiceFails() {
        FeignException.InternalServerError feignException = mock(FeignException.InternalServerError.class);
        when(feignException.status()).thenReturn(503);
        when(feignException.getMessage()).thenReturn("Service temporarily unavailable. Try again later.");

        when(userContext.getUserId()).thenReturn(currentUserId);
        when(projectServiceClient.getProject(projectId, userContext.getUserId()))
                .thenThrow(feignException);

        assertThrows(UserServiceUnavailableException.class,
                () -> authorValidationService.validateProjectExists(projectId));

        verify(projectServiceClient, times(3)).getProject(projectId, userContext.getUserId());
    }

    @Test
    @DisplayName("Should not retry and immediately throw EntityNotFoundException for 404")
    void validateProjectExists_shouldNotRetry_whenProjectNotFound() {
        when(userContext.getUserId()).thenReturn(currentUserId);
        when(projectServiceClient.getProject(projectId, userContext.getUserId()))
                .thenThrow(FeignException.NotFound.class);

        assertThrows(EntityNotFoundException.class,
                () -> authorValidationService.validateProjectExists(projectId));

        verify(projectServiceClient, times(1)).getProject(projectId, userContext.getUserId());
    }

}
