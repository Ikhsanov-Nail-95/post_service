package faang.school.postservice.publisher.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.exception.JsonSerializationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractRedisPublisher<T> {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final String channelName;

    public void publish(T eventType) {
        String message;
        try {
            message = objectMapper.writeValueAsString(eventType);
        } catch (JsonProcessingException e) {
            log.error("Error when serializing an object to a string JSON", e);
            throw new JsonSerializationException("Failed to serialize event: " + eventType, e);
        }
        redisTemplate.convertAndSend(channelName, message);
    }

}