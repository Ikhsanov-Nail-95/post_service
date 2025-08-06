package faang.school.postservice.publisher.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.event.PostViewEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class PostViewEventPublisher extends AbstractRedisEventPublisher<PostViewEvent> {
    public PostViewEventPublisher(ObjectMapper objectMapper,
                                  RedisTemplate<String, Object> redisTemplate,
                                  ChannelTopic postViewTopic) {
        super(objectMapper, redisTemplate, postViewTopic);
    }
}