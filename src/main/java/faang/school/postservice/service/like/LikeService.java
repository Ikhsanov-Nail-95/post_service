package faang.school.postservice.service.like;

import faang.school.postservice.client.dto.UserDto;
import faang.school.postservice.dto.request.LikeRequest;
import faang.school.postservice.dto.response.LikeResponse;
import faang.school.postservice.event.LikeEvent;
import faang.school.postservice.mapper.LikeMapper;
import faang.school.postservice.model.Like;
import faang.school.postservice.model.enums.LikeTargetType;
import faang.school.postservice.service.AuthorValidationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

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
        LikeResult result = strategy.handleLike(userId, likeRequest.getEntityId());

        Like like = result.like();
        LikeEvent event = LikeEvent.builder()
                .entityId(likeRequest.getEntityId())
                .targetType(likeRequest.getTargetType())
                .likeId(like.getId())
                .likedAt(like.getCreatedAt())
                .build();
        eventPublisher.publishEvent(event);

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