package faang.school.postservice.validator;

import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.exception.DataValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentValidation {

    private final UserContext userContext;

    public void ensureCurrentActorIsAuthor(Long authorId) {
        Long currentUserId = userContext.getUserId();

        if (!authorId.equals(currentUserId)) {
            throw new DataValidationException("Only the author user can perform this action.");
        }
    }

}