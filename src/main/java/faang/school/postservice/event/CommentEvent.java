package faang.school.postservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor
public class CommentEvent {

    private long commentId;
    private long authorId;
    private long postId;
    private ZonedDateTime commentedAt;

}