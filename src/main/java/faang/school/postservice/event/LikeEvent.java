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
    private long entityId;
    private LikeTargetType targetType;
    private long likeId;
    private ZonedDateTime likedAt;
}