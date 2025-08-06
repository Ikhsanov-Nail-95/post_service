package faang.school.postservice.publisher.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.exception.EventPublishException;
import faang.school.postservice.exception.JsonSerializationException;
import faang.school.postservice.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRedisEventPublisher<T> implements EventPublisher<T> {

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic channelTopic;

    @Override
    public void publish(T event) {
        String topic = channelTopic.getTopic();
        String payload;

        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize event [{}] for topic [{}]: {}",
                    event.getClass().getSimpleName(),
                    topic,
                    exception.getOriginalMessage(), exception);
            throw new JsonSerializationException(
                    "Unable to serialize event " + event.getClass().getSimpleName(), exception);
        }

        try {
            redisTemplate.convertAndSend(topic, payload);
            log.info("Published event [{}] to Redis topic [{}]",
                    event.getClass().getSimpleName(), topic);
        } catch (DataAccessException exception) {
            log.error("Failed to publish event [{}] to topic [{}]: {}",
                    event.getClass().getSimpleName(),
                    topic,
                    exception.getMessage(), exception);
            throw new EventPublishException(
                    "Failed to publish event to Redis topic " + topic, exception);
        }
    }

}