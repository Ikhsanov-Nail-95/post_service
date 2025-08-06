package faang.school.postservice.validator;

import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.exception.DataValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentValidationTest {

    @Mock
    private UserContext userContext;

    @InjectMocks
    private CommentValidation commentValidation;

    private final long currentUserId = 1L;

    @BeforeEach
    void setUp() {
        when(userContext.getUserId()).thenReturn(currentUserId);
    }

    @Test
    @DisplayName("Should pass when current user is the author")
    void ensureCurrentActorIsAuthor_shouldPass_whenUserIsAuthor() {
        assertDoesNotThrow(() -> commentValidation.ensureCurrentActorIsAuthor(currentUserId));
    }

    @Test
    @DisplayName("Should throw DataValidationException when current user is not the author")
    void ensureCurrentActorIsAuthor_shouldThrow_whenUserNotAuthor() {
        Long differentAuthorId = 42L;

        DataValidationException exception = assertThrows(
                DataValidationException.class,
                () -> commentValidation.ensureCurrentActorIsAuthor(differentAuthorId)
        );

        assertEquals("Only the author user can perform this action.", exception.getMessage());
    }

}