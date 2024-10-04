package faang.school.postservice.publisher.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisUserBannerPublisher extends AbstractRedisPublisher<List<Long>> {

    public RedisUserBannerPublisher(RedisTemplate<String, Object> redisTemplate,
                                    ObjectMapper objectMapper,
                                    @Value("${spring.data.redis.channels.user_ban_channel}")
                                    String channelName) {
        super(redisTemplate, objectMapper, channelName);
    }

}