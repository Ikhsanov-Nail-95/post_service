package faang.school.postservice.config.redis;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis.channels")
public class RedisChannelsConfig {

    @NotEmpty
    private String userBannerEvent;
    @NotEmpty
    private String postLikedEvent;
    @NotEmpty
    private String postCommentedEvent;

    @Bean
    public ChannelTopic userBannerEventTopic(){
        return new ChannelTopic(userBannerEvent);
    }

    @Bean
    public ChannelTopic postLikedEventTopic(){
        return new ChannelTopic(postLikedEvent);
    }

    @Bean
    public ChannelTopic postCommentedEventTopic() {
        return new ChannelTopic(postCommentedEvent);
    }
}