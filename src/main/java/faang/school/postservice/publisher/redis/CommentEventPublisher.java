package faang.school.postservice.publisher.redis;

import faang.school.postservice.event.CommentEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class CommentEventPublisher extends AbstractRedisEventPublisher<CommentEvent> {
    public CommentEventPublisher(RedisTemplate<String, Object> redisTemplate,
                                 ChannelTopic postCommentedEventTopic) {
        super(redisTemplate, postCommentedEventTopic);
    }
}