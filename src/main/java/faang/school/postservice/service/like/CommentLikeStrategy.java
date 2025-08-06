package faang.school.postservice.service.like;

import faang.school.postservice.client.dto.UserDto;
import faang.school.postservice.helper.UserFetcherHelper;
import faang.school.postservice.model.Comment;
import faang.school.postservice.model.Like;
import faang.school.postservice.repository.LikeRepository;
import faang.school.postservice.service.CommentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentLikeStrategy extends AbstractLikeStrategy {

    private final CommentService commentService;

    public CommentLikeStrategy(CommentService commentService,
                               LikeRepository likeRepository,
                               UserFetcherHelper userFetcherHelper) {
        super(likeRepository, userFetcherHelper);
        this.commentService = commentService;
    }

    @Override
    protected boolean alreadyLiked(long userId, long commentId) {
        return likeRepository.existsByUserIdAndCommentId(userId, commentId);
    }

    @Override
    protected LikeResult createAndSaveLikeWithAuthor(long userId, long commentId) {
        Comment comment = commentService.findCommentOrThrow(commentId);
        long authorId = comment.getAuthorId();

        Like like = likeRepository.save(Like.builder()
                .userId(userId)
                .comment(comment)
                .build());

        return new LikeResult(like, authorId);
    }

    @Override
    protected void deleteLike(long userId, long commentId) {
        likeRepository.deleteByUserIdAndCommentId(userId, commentId);
    }

    @Override
    public List<UserDto> getUsersWhoLiked(long commentId) {
        List<Long> userIds = likeRepository.findUserIdsByCommentId(commentId);
        return userFetcherHelper.fetchUsersInBatches(userIds);
    }

}