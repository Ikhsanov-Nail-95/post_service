package faang.school.postservice.service;

import faang.school.postservice.client.UserServiceClient;
import faang.school.postservice.config.context.UserContext;
import faang.school.postservice.dto.like.LikeDto;
import faang.school.postservice.dto.like.LikeEventDto;
import faang.school.postservice.dto.UserDto;
import faang.school.postservice.mapper.LikeMapper;
import faang.school.postservice.model.Comment;
import faang.school.postservice.model.Like;
import faang.school.postservice.model.Post;
import faang.school.postservice.publisher.redis.RedisLikeEventPublisher;
import faang.school.postservice.repository.LikeRepository;
import faang.school.postservice.validator.LikeValidation;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final PostService postService;
    private final CommentService commentService;
    private final LikeRepository likeRepository;
    private final LikeValidation likeValidation;
    private final LikeMapper likeMapper;
    private final UserServiceClient userServiceClient;
    private final UserContext userContext;
    private final RedisLikeEventPublisher redisLikeEventPublisher;

    public LikeDto likePost(LikeDto likeDto) {
        Post checkPost = postService.existsPost(likeDto.getPostId());
        userServiceClient.getUser(likeDto.getUserId());
        likeValidation.verifyUniquenessLikePost(likeDto.getPostId(), likeDto.getUserId());
        Like like = Like.builder()
                .id(likeDto.getUserId())
                .post(checkPost)
                .build();

        LikeEventDto likeEventDto = buildLikeEvent(likeDto);
        LikeEventDto likeEventDtoPost = likeEventDto.toBuilder()
                .authorPostId(checkPost.getAuthorId())
                .postId(checkPost.getId())
                .build();

        redisLikeEventPublisher.publish(likeEventDtoPost);

        return likeMapper.toDto(likeRepository.save(like));
    }

    public void deleteLikeFromPost(Long postId) {
        UserDto userDto = getUserFromUserService();
        likeRepository.deleteByPostIdAndUserId(postId, userDto.getId());
    }

    public LikeDto likeComment(LikeDto likeDto) {
        Comment checkComment = commentService.findCommentById(likeDto.getCommentId());
        userServiceClient.getUser(likeDto.getUserId());
        likeValidation.verifyUniquenessLikeComment(likeDto.getCommentId(), likeDto.getUserId());
        Like like = Like.builder()
                .id(likeDto.getUserId())
                .comment(checkComment)
                .build();

        LikeEventDto likeEventDto = buildLikeEvent(likeDto);
        LikeEventDto likeEventDtoComment = likeEventDto.toBuilder()
                .authorCommentId(checkComment.getAuthorId())
                .commentId(checkComment.getId())
                .build();

        redisLikeEventPublisher.publish(likeEventDtoComment);

        return likeMapper.toDto(likeRepository.save(like));
    }

    public void deleteLikeFromComment(Long commentId) {
        UserDto userDto = getUserFromUserService();
        likeRepository.deleteByCommentIdAndUserId(commentId, userDto.getId());
    }

    private UserDto getUserFromUserService() {
        try {
            return userServiceClient.getUser(userContext.getUserId());
        } catch (FeignException e) {
            throw new EntityNotFoundException(e.getMessage());
        }
    }

    public LikeEventDto buildLikeEvent(LikeDto likeDto) {
        return LikeEventDto.builder()
                .authorLikeId(likeDto.getUserId())
                .createdAt(LocalDateTime.now())
                .build();
    }
}