package faang.school.postservice.listener.redis;

import faang.school.postservice.event.CommentEvent;
import faang.school.postservice.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class CommentEventAfterCommitListener {

    private final EventPublisher<CommentEvent> publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(CommentEvent event) {
        publisher.publish(event);
    }

}