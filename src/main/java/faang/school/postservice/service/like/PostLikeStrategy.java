package faang.school.postservice.service.like;

import faang.school.postservice.client.dto.UserDto;
import faang.school.postservice.helper.UserFetcherHelper;
import faang.school.postservice.model.Like;
import faang.school.postservice.model.Post;
import faang.school.postservice.repository.LikeRepository;
import faang.school.postservice.service.PostService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostLikeStrategy extends AbstractLikeStrategy {
    private final PostService postService;

    public PostLikeStrategy(PostService postService,
                            LikeRepository likeRepository,
                            UserFetcherHelper userFetcherHelper) {
        super(likeRepository, userFetcherHelper);
        this.postService = postService;
    }

    @Override
    protected boolean alreadyLiked(long userId, long postId) {
        return likeRepository.existsByUserIdAndPostId(userId, postId);
    }

    @Override
    protected Like createAndSaveLike(long userId, long postId) {
        Post post = postService.findPostOrThrow(postId);
        return likeRepository.save(Like.builder()
                .userId(userId)
                .post(post)
                .build());
    }

    @Override
    protected Optional<Like> findLike(long userId, long postId) {
        return likeRepository.findByUserIdAndPostId(userId, postId);
    }

    @Override
    protected void deleteLike(long userId, long postId) {
        likeRepository.deleteByUserIdAndPostId(userId, postId);
    }

    @Override
    public List<UserDto> getUsersWhoLiked(long postId) {
        List<Long> userIds = likeRepository.findUserIdsByPostId(postId);
        return userFetcherHelper.fetchUsersInBatches(userIds);
    }
}