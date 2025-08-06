package faang.school.postservice.service;

import faang.school.postservice.client.ProjectServiceClient;
import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.client.dto.ProjectDto;
import faang.school.postservice.client.dto.UserDto;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.exception.UserServiceUnavailableException;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorValidationServiceTest {

    @Mock private UserServiceClient userServiceClient;
    @Mock private ProjectServiceClient projectServiceClient;
    @Mock private UserContext userContext;

    @InjectMocks private AuthorValidationService authorValidationService;

    private final long userId = 1L;
    private final long projectId = 2L;
    private final long currentUserId = 3L;

    @Test
    @DisplayName("Should validate user existence when user is found")
    void validateUserExists_shouldPass_whenUserExists() {
        when(userServiceClient.getUser(userId)).thenReturn(new UserDto());

        assertDoesNotThrow(() -> authorValidationService.validateUserExists(userId));

        verify(userServiceClient).getUser(userId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user not found")
    void validateUserExists_shouldThrow_whenUserNotFound() {
        when(userServiceClient.getUser(userId)).thenThrow(FeignException.NotFound.class);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> authorValidationService.validateUserExists(userId));

        assertEquals("User not found.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw UserServiceUnavailableException when user service fails")
    void validateUserExists_shouldThrow_whenUserServiceFails() {
        FeignException.InternalServerError feignException =
                mock(FeignException.InternalServerError.class);

        when(feignException.status()).thenReturn(503);
        when(feignException.getMessage()).thenReturn("Internal Server Error");
        when(userServiceClient.getUser(userId)).thenThrow(feignException);

        UserServiceUnavailableException ex = assertThrows(UserServiceUnavailableException.class,
                () -> authorValidationService.validateUserExists(userId));

        assertEquals("Service temporarily unavailable. Try again later.", ex.getMessage());
    }

    @Test
    @DisplayName("Should validate project existence when project is found")
    void validateProjectExists_shouldPass_whenProjectExists() {
        when(userContext.getUserId()).thenReturn(currentUserId);
        when(projectServiceClient.getProject(projectId, currentUserId)).thenReturn(new ProjectDto());

        assertDoesNotThrow(() -> authorValidationService.validateProjectExists(projectId));

        verify(projectServiceClient).getProject(projectId, currentUserId);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when project not found")
    void validateProjectExists_shouldThrow_whenProjectNotFound() {
        when(userContext.getUserId()).thenReturn(currentUserId);
        when(projectServiceClient.getProject(projectId, currentUserId))
                .thenThrow(FeignException.NotFound.class);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> authorValidationService.validateProjectExists(projectId));

        assertEquals("Project not found.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw UserServiceUnavailableException when project service fails")
    void validateProjectExists_shouldThrow_whenProjectServiceFails() {
        when(userContext.getUserId()).thenReturn(currentUserId);

        FeignException.InternalServerError feignException =
                mock(FeignException.InternalServerError.class);
        when(feignException.status()).thenReturn(503);
        when(feignException.getMessage()).thenReturn("Internal Server Error");

        when(projectServiceClient.getProject(projectId, currentUserId))
                .thenThrow(feignException);

        UserServiceUnavailableException ex = assertThrows(UserServiceUnavailableException.class,
                () -> authorValidationService.validateProjectExists(projectId));

        assertEquals("Service temporarily unavailable. Try again later.", ex.getMessage());
    }

}