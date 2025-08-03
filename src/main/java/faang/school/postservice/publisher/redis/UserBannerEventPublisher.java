package faang.school.postservice.publisher.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserBannerEventPublisher extends AbstractRedisEventPublisher<List<Long>> {
    public UserBannerEventPublisher(RedisTemplate<String, Object> redisTemplate,
                                    ChannelTopic userBannerEventTopic) {
        super(redisTemplate, userBannerEventTopic);
    }
}