package faang.school.postservice.validator;

import faang.school.postservice.config.context.ProjectContext;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.request.PostCreateRequest;
import faang.school.postservice.exception.DataValidationException;
import faang.school.postservice.model.Post;
import faang.school.postservice.service.AuthorValidationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostValidatorTest {

    @Mock private UserContext userContext;
    @Mock private ProjectContext projectContext;
    @Mock private AuthorValidationService authorValidationService;

    @InjectMocks private PostValidator postValidator;

    private PostCreateRequest postCreateRequest;
    private Post post;

    private long userId;
    private long projectId;
    private long currentUserId;
    private long anotherProjectId;

    @BeforeEach
    void setUp() {
        postCreateRequest = new PostCreateRequest();
        post = new Post();

        userId = 1L;
        projectId = 2L;
        currentUserId = 3L;
        anotherProjectId = 4L;
    }

    @Test
    @DisplayName("Should throw DataValidationException when both userId and projectId authors are null")
    void validateAuthor_shouldThrow_whenBothAuthorsNull() {
        postCreateRequest.setAuthorId(null);
        postCreateRequest.setProjectId(null);

        DataValidationException exception = assertThrows(DataValidationException.class,
                () -> postValidator.validateAuthor(postCreateRequest));

        assertEquals("Post must have an author: either a user or a project.", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw DataValidationException when both userId and projectId are provided")
    void validateAuthor_shouldThrow_whenBothAuthorsPresent() {
        postCreateRequest.setAuthorId(userId);
        postCreateRequest.setProjectId(projectId);

        DataValidationException exception = assertThrows(DataValidationException.class,
                () -> postValidator.validateAuthor(postCreateRequest));

        assertEquals("Post cannot have both a user and a project as authors.", exception.getMessage());
    }

    @Test
    @DisplayName("Should validate user existence when only userId is provided")
    void validateAuthor_shouldValidateUser_whenOnlyUserIdIsProvided() {
        postCreateRequest.setAuthorId(userId);
        postCreateRequest.setProjectId(null);

        doNothing().when(authorValidationService).validateUserExists(userId);

        postValidator.validateAuthor(postCreateRequest);

        verify(authorValidationService, times(1)).validateUserExists(userId);
        verify(authorValidationService, never()).validateProjectExists(anyLong());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user not found")
    void validateAuthor_shouldThrow_whenUserNotFound() {
        postCreateRequest.setAuthorId(userId);
        postCreateRequest.setProjectId(null);

        doThrow(new EntityNotFoundException("User not found."))
                .when(authorValidationService).validateUserExists(userId);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> postValidator.validateAuthor(postCreateRequest));

        assertEquals("User not found.", ex.getMessage());
    }

    @Test
    @DisplayName("Should validate project existence when only projectId is provided")
    void validateAuthor_shouldValidateProject_whenOnlyProjectIdIsProvided() {
        postCreateRequest.setAuthorId(null);
        postCreateRequest.setProjectId(projectId);

        doNothing().when(authorValidationService).validateProjectExists(projectId);

        postValidator.validateAuthor(postCreateRequest);

        verify(authorValidationService, times(1)).validateProjectExists(projectId);
        verify(authorValidationService, never()).validateUserExists(anyLong());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when project not found")
    void validateAuthor_shouldThrow_whenProjectNotFound() {
        postCreateRequest.setAuthorId(null);
        postCreateRequest.setProjectId(projectId);

        doThrow(new EntityNotFoundException("Project not found."))
                .when(authorValidationService).validateProjectExists(projectId);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> postValidator.validateAuthor(postCreateRequest));
        assertEquals("Project not found.", ex.getMessage());
    }

    @Test
    @DisplayName("Should pass validation when current user is the author")
    void ensureCurrentActorIsAuthor_shouldPass_whenCurrentUserIsAuthor() {
        post.setAuthorId(userId);
        post.setProjectId(null);

        when(userContext.getUserId()).thenReturn(userId);

        assertDoesNotThrow(() -> postValidator.ensureCurrentActorIsAuthor(post));
    }

    @Test
    @DisplayName("Should throw DataValidationException when current user is not the author")
    void ensureCurrentActorIsAuthor_shouldThrow_whenUserNotAuthor() {
        post.setAuthorId(userId);
        post.setProjectId(null);

        when(userContext.getUserId()).thenReturn(currentUserId);

        DataValidationException ex = assertThrows(DataValidationException.class,
                () -> postValidator.ensureCurrentActorIsAuthor(post));
        assertEquals("Only the author user can perform this action.", ex.getMessage());
    }

    @Test
    @DisplayName("Should pass validation when current project is the author")
    void ensureCurrentActorIsAuthor_shouldPass_whenCurrentProjectIsAuthor() {
        post.setAuthorId(null);
        post.setProjectId(projectId);

        when(projectContext.getProjectId()).thenReturn(projectId);

        assertDoesNotThrow(() -> postValidator.ensureCurrentActorIsAuthor(post));
    }

    @Test
    @DisplayName("Should throw DataValidationException when current project is not the author")
    void ensureCurrentActorIsAuthor_shouldThrow_whenProjectNotAuthor() {
        post.setAuthorId(null);
        post.setProjectId(anotherProjectId);

        when(projectContext.getProjectId()).thenReturn(projectId);

        DataValidationException ex = assertThrows(DataValidationException.class,
                () -> postValidator.ensureCurrentActorIsAuthor(post));
        assertEquals("Only the author project can perform this action.", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw DataValidationException when neither user nor project is set")
    void ensureCurrentActorIsAuthor_shouldThrow_whenNoAuthorOrProject() {
        post.setAuthorId(userId);
        post.setProjectId(projectId);

        DataValidationException ex = assertThrows(DataValidationException.class,
                () -> postValidator.ensureCurrentActorIsAuthor(post));
        assertEquals("Post must have exactly one author: either a user or a project.", ex.getMessage());
    }

    @Test
    @DisplayName("Should pass validation when the post has not been deleted")
    void ensureNotDeleted_shouldPass_whenPostNotDeleted() {
        post.setDeleted(false);

        assertDoesNotThrow(() -> postValidator.ensureNotDeleted(post));
    }

    @Test
    @DisplayName("Should throw DataValidationException when a post is marked as deleted")
    void ensureNotDeleted_shouldThrow_whenPostIsDeleted() {
        post.setDeleted(true);

        DataValidationException ex = assertThrows(DataValidationException.class,
                () -> postValidator.ensureNotDeleted(post));
        assertEquals("The post has already been deleted.", ex.getMessage());
    }

    @Test
    @DisplayName("Should pass validation when the post has not been published")
    void ensureNotPublished_shouldPass_whenPostNotPublished() {
        post.setPublished(false);

        assertDoesNotThrow(() -> postValidator.ensureNotPublished(post));
    }

    @Test
    @DisplayName("Should throw DataValidationException when post is already published")
    void ensureNotPublished_shouldThrow_whenPostPublished() {
        post.setPublished(true);

        DataValidationException ex = assertThrows(DataValidationException.class,
                () -> postValidator.ensureNotPublished(post));
        assertEquals("The post cannot be published again.", ex.getMessage());
    }

}