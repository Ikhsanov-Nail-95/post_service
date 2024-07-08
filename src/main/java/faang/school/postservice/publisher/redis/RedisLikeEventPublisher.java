package faang.school.postservice.publisher.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.dto.like.LikeEventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisLikeEventPublisher extends AbstractRedisPublisher<LikeEventDto> {

    public RedisLikeEventPublisher(RedisTemplate<String, Object> redisTemplate,
                                   ObjectMapper objectMapper,
                                   @Value("${spring.data.redis.channels.notification_like_channel}")
                                   String channelName) {
        super(redisTemplate, objectMapper, channelName);
    }

}