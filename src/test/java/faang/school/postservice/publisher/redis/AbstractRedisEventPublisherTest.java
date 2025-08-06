package faang.school.postservice.publisher.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.exception.JsonSerializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractRedisEventPublisherTest {

    @Mock
    private ObjectMapper objectMapper;
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
        publisher = new AbstractRedisEventPublisher<>(objectMapper, redisTemplate, channelTopic) {};
    }

    @Test
    @DisplayName("Should publish event to Redis channel")
    void publish_shouldSendEventToRedis() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(event)).thenReturn(event);
        publisher.publish(event);
        verify(redisTemplate).convertAndSend(topic, event);
    }

    @Test
    @DisplayName("Should throw JsonSerializationException when RedisTemplate throws SerializationException")
    void publish_shouldThrowException_whenSerializationFails() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(event)).thenThrow(new JsonProcessingException("Serialization failed") {});

        JsonSerializationException exception = assertThrows(
                JsonSerializationException.class,
                () -> publisher.publish(event)
        );

        assertTrue(exception.getMessage().contains("Unable to serialize event"));
    }
}