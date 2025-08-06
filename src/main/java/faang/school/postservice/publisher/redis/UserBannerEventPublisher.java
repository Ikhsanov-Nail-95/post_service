package faang.school.postservice.publisher.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserBannerEventPublisher extends AbstractRedisEventPublisher<List<Long>> {
    public UserBannerEventPublisher(ObjectMapper objectMapper,
                                    RedisTemplate<String, Object> redisTemplate,
                                    ChannelTopic userBannerTopic) {
        super(objectMapper, redisTemplate, userBannerTopic);
    }
}