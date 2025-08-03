package faang.school.postservice.service.like;

import faang.school.postservice.client.dto.UserDto;
import faang.school.postservice.dto.request.LikeRequest;
import faang.school.postservice.dto.response.LikeResponse;
import faang.school.postservice.event.LikeEvent;
import faang.school.postservice.mapper.LikeMapper;
import faang.school.postservice.model.Like;
import faang.school.postservice.model.enums.LikeTargetType;
import faang.school.postservice.publisher.EventPublisher;
import faang.school.postservice.service.AuthorValidationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeMapper likeMapper;
    private final PostLikeStrategy postLikeStrategy;
    private final CommentLikeStrategy commentLikeStrategy;
    private final AuthorValidationService authorValidationService;
    private final EventPublisher<LikeEvent> likeEventPublisher;

    private final Map<LikeTargetType, LikeStrategy> strategies = new EnumMap<>(LikeTargetType.class);

    @PostConstruct
    void initStrategies() {
        strategies.put(LikeTargetType.POST, postLikeStrategy);
        strategies.put(LikeTargetType.COMMENT, commentLikeStrategy);
    }

    @Transactional
    public LikeResponse likeEntity(long userId, LikeRequest likeRequest) {
        authorValidationService.validateUserExists(userId);

        LikeStrategy strategy = getStrategyOrThrow(likeRequest.getTargetType());
        Like like = strategy.handleLike(userId, likeRequest.getEntityId());

        LikeEvent event = LikeEvent.builder()
                .likeId(like.getId())
                .userId(userId)
                .entityId(likeRequest.getEntityId())
                .likeTargetType(likeRequest.getTargetType())
                .likedAt(like.getCreatedAt())
                .build();
        likeEventPublisher.publish(event);

        return likeMapper.toResponse(like);
    }

    @Transactional
    public void unlikeEntity(long userId, LikeRequest likeRequest) {
        authorValidationService.validateUserExists(userId);

        LikeStrategy strategy = getStrategyOrThrow(likeRequest.getTargetType());
        strategy.handleUnlike(userId, likeRequest.getEntityId());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersWhoLikedEntity(LikeTargetType targetType, long entityId) {
        LikeStrategy strategy = getStrategyOrThrow(targetType);
        return strategy.getUsersWhoLiked(entityId);
    }

    private LikeStrategy getStrategyOrThrow(LikeTargetType likeTargetType) {
        LikeStrategy strategy = strategies.get(likeTargetType);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported Like target type: " + likeTargetType);
        }
        return strategy;
    }
}