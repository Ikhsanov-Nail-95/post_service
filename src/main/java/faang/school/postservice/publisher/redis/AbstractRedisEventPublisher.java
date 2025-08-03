package faang.school.postservice.publisher.redis;

import faang.school.postservice.exception.JsonSerializationException;
import faang.school.postservice.publisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.serializer.SerializationException;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractRedisEventPublisher<T> implements EventPublisher<T> {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic channelTopic;

    @Override
    public void publish(T eventType) {
        try {
            redisTemplate.convertAndSend(channelTopic.getTopic(), eventType);
        } catch (SerializationException exception) {
            log.error("Failed to publish [{}] to topic [{}]. Serialization error occurred",
                    eventType.getClass().getSimpleName(),
                    channelTopic.getTopic(),
                    exception);
            throw new JsonSerializationException("Failed to serialize event: " + eventType + " to JSON");
        }
    }
}