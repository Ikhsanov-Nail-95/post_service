package faang.school.postservice.publisher.redis;

import faang.school.postservice.event.LikeEvent;
import faang.school.postservice.exception.JsonSerializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.serializer.SerializationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeEventPublisherTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ChannelTopic channelTopic;

    @InjectMocks
    private LikeEventPublisher likeEventPublisher;

    private LikeEvent likeEvent;

    private final String topic = "like-topic";

    @BeforeEach
    void setUp() {
        when(channelTopic.getTopic()).thenReturn(topic);
        likeEvent = LikeEvent.builder().build();
    }

    @Test
    @DisplayName("Should serialize LikeEvent to JSON and send it to Redis channel")
    void publish_shouldPublishedToRedis_whenValidEvent() {
        likeEventPublisher.publish(likeEvent);

        verify(redisTemplate, times(1)).convertAndSend(topic, likeEvent);
    }

    @Test
    @DisplayName("Should throw JsonSerializationException when RedisTemplate throws SerializationException")
    void publish_shouldThrowException_whenSerializationFails() {
        SerializationException serializationException = new SerializationException("test serialization failure");
        doThrow(serializationException)
                .when(redisTemplate)
                .convertAndSend(topic, likeEvent);

        JsonSerializationException thrown = assertThrows(JsonSerializationException.class,
                () -> likeEventPublisher.publish(likeEvent));

        assertEquals("Failed to serialize event: " + likeEvent + " to JSON", thrown.getMessage());
    }
}