package faang.school.postservice.listener.redis;

import faang.school.postservice.event.LikeEvent;
import faang.school.postservice.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class LikeEventAfterCommitListener {

    private final EventPublisher<LikeEvent> publisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommit(LikeEvent event) {
        publisher.publish(event);
    }

}