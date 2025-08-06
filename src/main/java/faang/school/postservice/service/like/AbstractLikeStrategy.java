package faang.school.postservice.service.like;

import faang.school.postservice.exception.DataValidationException;
import faang.school.postservice.helper.UserFetcherHelper;
import faang.school.postservice.repository.LikeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractLikeStrategy implements LikeStrategy {

    protected final LikeRepository likeRepository;
    protected final UserFetcherHelper userFetcherHelper;

    protected abstract boolean alreadyLiked(long userId, long entityId);

    protected abstract LikeResult createAndSaveLikeWithAuthor(long userId, long entityId);

    protected abstract void deleteLike(long userId, long entityId);

    @Override
    public LikeResult handleLike(long userId, long entityId) {
        if (alreadyLiked(userId, entityId)) {
            throw new DataValidationException("Already liked");
        }
        return createAndSaveLikeWithAuthor(userId, entityId);
    }

    @Override
    public void handleUnlike(long userId, long entityId) {
        if (!alreadyLiked(userId, entityId)) {
            throw new EntityNotFoundException("Like not found");
        }
        deleteLike(userId, entityId);
    }

}