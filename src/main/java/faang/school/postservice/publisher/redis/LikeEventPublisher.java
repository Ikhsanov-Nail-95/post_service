package faang.school.postservice.publisher.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.event.LikeEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class LikeEventPublisher extends AbstractRedisEventPublisher<LikeEvent> {
    public LikeEventPublisher(ObjectMapper objectMapper,
                              RedisTemplate<String, Object> redisTemplate,
                              ChannelTopic likeTopic) {
        super(objectMapper, redisTemplate, likeTopic);
    }
}