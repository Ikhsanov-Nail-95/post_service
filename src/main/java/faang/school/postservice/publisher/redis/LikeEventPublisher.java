package faang.school.postservice.publisher.redis;

import faang.school.postservice.event.LikeEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class LikeEventPublisher extends AbstractRedisEventPublisher<LikeEvent> {
    public LikeEventPublisher(RedisTemplate<String, Object> redisTemplate,
                              ChannelTopic postLikedEventTopic) {
        super(redisTemplate, postLikedEventTopic);
    }
}