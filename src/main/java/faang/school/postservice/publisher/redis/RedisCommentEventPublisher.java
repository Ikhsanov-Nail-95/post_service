package faang.school.postservice.publisher.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.dto.comment.CommentEventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCommentEventPublisher extends AbstractRedisPublisher<CommentEventDto> {

    public RedisCommentEventPublisher(RedisTemplate<String, Object> redisTemplate,
                                      ObjectMapper objectMapper,
                                      @Value("${spring.data.redis.channels.notification_comment_channel}")
                                 String channelName) {
        super(redisTemplate, objectMapper, channelName);

    }

}