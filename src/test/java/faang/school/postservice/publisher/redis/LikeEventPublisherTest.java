package faang.school.postservice.publisher.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.event.LikeEvent;
import faang.school.postservice.exception.EventPublishException;
import faang.school.postservice.exception.JsonSerializationException;
import faang.school.postservice.model.enums.LikeTargetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeEventPublisherTest {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ChannelTopic channelTopic;
    @InjectMocks
    private LikeEventPublisher likeEventPublisher;

    private final String topic = "like-topic";
    private final String payload = "{\"dummy\":\"json\"}";
    private final LikeEvent likeEvent = LikeEvent.builder()
            .entityId(1L)
            .targetType(LikeTargetType.POST)
            .likeId(2L)
            .likedAt(ZonedDateTime.now()).build();

    @BeforeEach
    void setUp() {
        when(channelTopic.getTopic()).thenReturn(topic);
    }

    @Test
    @DisplayName("Should serialize LikeEvent to JSON and send it to Redis channel")
    void publish_shouldPublishSerializedJsonToRedis_whenValidEvent() throws Exception {
        when(objectMapper.writeValueAsString(likeEvent)).thenReturn(payload);

        likeEventPublisher.publish(likeEvent);

        verify(objectMapper, times(1)).writeValueAsString(likeEvent);
        verify(redisTemplate, times(1)).convertAndSend(topic, payload);
    }

    @Test
    @DisplayName("Should throw JsonSerializationException when serialization fails")
    void publish_shouldThrowJsonSerializationException_whenJsonProcessingException() throws Exception {
        JsonProcessingException jpe = new JsonProcessingException("fail") {};
        when(objectMapper.writeValueAsString(likeEvent)).thenThrow(jpe);

        JsonSerializationException ex = assertThrows(JsonSerializationException.class,
                () -> likeEventPublisher.publish(likeEvent));
        assertTrue(ex.getMessage().contains("Unable to serialize event"));
        verify(redisTemplate, never()).convertAndSend(anyString(), any());
    }

    @Test
    @DisplayName("Should throw EventPublishException when Redis publish fails")
    void publish_shouldThrowEventPublishException_whenRedisFails() throws Exception {
        when(objectMapper.writeValueAsString(likeEvent)).thenReturn(payload);
        DataAccessException dae = new DataAccessException("redis down") {
        };
        doThrow(dae).when(redisTemplate).convertAndSend(topic, payload);

        EventPublishException ex = assertThrows(EventPublishException.class,
                () -> likeEventPublisher.publish(likeEvent));
        assertTrue(ex.getMessage().contains("Failed to publish event to Redis topic"));
        verify(objectMapper, times(1)).writeValueAsString(likeEvent);
        verify(redisTemplate, times(1)).convertAndSend(topic, payload);
    }

}