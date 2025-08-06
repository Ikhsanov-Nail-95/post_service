package faang.school.postservice.validator;

import faang.school.postservice.config.context.ProjectContext;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.request.PostCreateRequest;
import faang.school.postservice.exception.DataValidationException;
import faang.school.postservice.model.Post;
import faang.school.postservice.service.AuthorValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PostValidator {

    private final UserContext userContext;
    private final ProjectContext projectContext;
    private final AuthorValidationService authorValidationService;


    public void validateAuthor(PostCreateRequest postCreateRequest) {
        Long userId = postCreateRequest.getAuthorId();
        Long projectId = postCreateRequest.getProjectId();

        if (userId == null && projectId == null) {
            throw new DataValidationException("Post must have an author: either a user or a project.");
        }
        if (userId != null && projectId != null) {
            throw new DataValidationException("Post cannot have both a user and a project as authors.");
        }

        if (userId != null) {
            authorValidationService.validateUserExists(userId);
        }
        if (projectId != null) {
            authorValidationService.validateProjectExists(projectId);
        }
    }

    public void ensureCurrentActorIsAuthor(Post post) {
        Long userId = post.getAuthorId();
        Long projectId = post.getProjectId();

        if (userId != null && projectId == null) {
            if (!userId.equals(userContext.getUserId())) {
                throw new DataValidationException("Only the author user can perform this action.");
            }
            return;
        }

        if (projectId != null && userId == null) {
            if (!projectId.equals(projectContext.getProjectId())) {
                throw new DataValidationException("Only the author project can perform this action.");
            }
            return;
        }

        throw new DataValidationException("Post must have exactly one author: either a user or a project.");
    }

    public void ensureNotDeleted(Post post) {
        if (post.isDeleted()) {
            throw new DataValidationException("The post has already been deleted.");
        }
    }

    public void ensureNotPublished(Post post) {
        if (post.isPublished()) {
            throw new DataValidationException("The post cannot be published again.");
        }
    }

}