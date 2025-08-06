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
    private String postViewEvent;
    @NotEmpty
    private String commentEvent;
    @NotEmpty
    private String likeEvent;
    @NotEmpty
    private String userBannerEvent;

    @Bean
    public ChannelTopic postViewTopic() {
        return new ChannelTopic(postViewEvent);
    }

    @Bean
    public ChannelTopic commentTopic() {
        return new ChannelTopic(commentEvent);
    }

    @Bean
    public ChannelTopic likeTopic() {
        return new ChannelTopic(likeEvent);
    }

    @Bean
    public ChannelTopic userBannerTopic() {
        return new ChannelTopic(userBannerEvent);
    }
}