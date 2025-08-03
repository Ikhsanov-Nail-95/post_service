package faang.school.postservice.publisher.redis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.serializer.SerializationException;
import faang.school.postservice.exception.JsonSerializationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractRedisEventPublisherTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ChannelTopic channelTopic;

    private AbstractRedisEventPublisher<String> publisher;

    private final String topic = "test-topic";
    private final String event = "test-event";

    @BeforeEach
    void setUp() {
        when(channelTopic.getTopic()).thenReturn(topic);
        publisher = new AbstractRedisEventPublisher<>(redisTemplate, channelTopic) {};
    }

    @Test
    @DisplayName("Should publish event to Redis channel")
    void publish_shouldSendEventToRedis() {
        publisher.publish(event);
        verify(redisTemplate).convertAndSend(topic, event);
    }

    @Test
    @DisplayName("Should throw JsonSerializationException when RedisTemplate throws SerializationException")
    void publish_shouldThrowException_whenSerializationFails() {
        SerializationException serializationException = new SerializationException("test serialization failure");

        doThrow(serializationException)
                .when(redisTemplate)
                .convertAndSend(topic, event);

        JsonSerializationException thrown = assertThrows(
                JsonSerializationException.class,
                () -> publisher.publish(event)
        );

        assertEquals("Failed to serialize event: " + event + " to JSON", thrown.getMessage());
    }
}