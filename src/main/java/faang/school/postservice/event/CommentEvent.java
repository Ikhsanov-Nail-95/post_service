package faang.school.postservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CommentEvent {
    private long postId;
    private long commentId;
    private ZonedDateTime commentedAt;
}