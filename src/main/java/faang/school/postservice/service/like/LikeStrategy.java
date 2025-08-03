package faang.school.postservice.service.like;

import faang.school.postservice.client.dto.UserDto;
import faang.school.postservice.model.Like;

import java.util.List;

public interface LikeStrategy {
    Like handleLike(long userId, long entityId);
    void handleUnlike(long userId, long entityId);

    List<UserDto> getUsersWhoLiked(long entityId);
}