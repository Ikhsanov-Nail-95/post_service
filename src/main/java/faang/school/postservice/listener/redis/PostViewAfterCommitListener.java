package faang.school.postservice.listener.redis;

import faang.school.postservice.event.PostViewEvent;
import faang.school.postservice.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class PostViewAfterCommitListener {

    private final EventPublisher<PostViewEvent> publisher;

    @Async("postAnalyticsExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(PostViewEvent event) {
        publisher.publish(event);
    }

}