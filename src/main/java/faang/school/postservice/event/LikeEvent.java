package faang.school.postservice.event;

import faang.school.postservice.model.enums.LikeTargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class LikeEvent {
    private long likeId;
    private long userId;
    private long entityId;
    private LikeTargetType likeTargetType;
    private ZonedDateTime likedAt;
}