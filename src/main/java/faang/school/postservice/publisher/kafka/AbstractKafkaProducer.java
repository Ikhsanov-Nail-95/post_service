package faang.school.postservice.publisher.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractKafkaProducer<T> {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topicName;

    //TODO: Пока думаю, возможно неправильно делаю, не совсем понимаю что мне надо отправлять,
    // надо смотреть ролики по урокам ньюсфида
    public void sendMessage(T eventType) {
        String message;
        try {
            message = objectMapper.writeValueAsString(eventType);
        } catch (JsonProcessingException e) {
            log.error("Error when serializing an object to a string JSON", e);
            throw new RuntimeException(e);
        }
        kafkaTemplate.send(topicName, message);
    }

    //TODO: Пока думаю что лучше использовать или может совместить
    public void sendMessage(String message) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, message);

        future.whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Unable to send message = [{}] due to : {}", message, exception.getMessage());
            }
        });
    }
}