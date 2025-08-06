package faang.school.postservice.helper;

import faang.school.postservice.event.PostViewEvent;
import faang.school.postservice.model.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
public class PostAnalyticsEventHelper {

    private final ApplicationEventPublisher eventPublisher;

    public void publishPostViewEvent(Post post, long viewerUserId) {
        PostViewEvent ev = PostViewEvent.builder()
                .postId(post.getId())
                .viewerUserId(viewerUserId)
                .viewedAt(ZonedDateTime.now())
                .build();

        eventPublisher.publishEvent(ev);
    }

    public void publishPostViewEvents(List<Post> posts, long viewerUserId) {
        posts.forEach(post -> publishPostViewEvent(post, viewerUserId));
    }

}