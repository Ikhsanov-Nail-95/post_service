package faang.school.postservice.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaHandler;

@Slf4j
//@Component
public class KafkaExceptionHandler {

    //TODO: надо подумать, можно пригодится https://howtodoinjava.com/spring-boot/apache-kafka-using-spring-boot/#5-4-error-handling-and-recovery
    @KafkaHandler
    public void handleKafkaException(KafkaException exception) {

        // Handle the KafkaException
        // ...
    }

}