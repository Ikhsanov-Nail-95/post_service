package faang.school.postservice.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.postservice.dto.like.LikeEventDto;
import faang.school.postservice.publisher.redis.RedisLikeEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RedisLikeEventPublisherTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private LikeEventDto likeEventDto;
    @InjectMocks
    private RedisLikeEventPublisher publisher;

    private String json;

    @BeforeEach
    public void setUp() {
        String topic = "profile_search_channel";
        publisher = new RedisLikeEventPublisher(redisTemplate, objectMapper, topic);
        json = "JSON";
    }

    @Test
    @DisplayName("Checking the correctness works of the method")
    public void testPublish() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(likeEventDto)).thenReturn(json);

        publisher.publish(likeEventDto);

        verify(objectMapper).writeValueAsString(likeEventDto);
        verify(redisTemplate).convertAndSend(anyString(), eq(json));
    }

    @Test
    @DisplayName("Checking that the method throws an exception")
    public void testPublish_FailedSerialization() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(likeEventDto)).thenThrow(JsonProcessingException.class);

        assertThrows(RuntimeException.class, () -> publisher.publish(likeEventDto));

        verify(objectMapper).writeValueAsString(likeEventDto);
    }
}