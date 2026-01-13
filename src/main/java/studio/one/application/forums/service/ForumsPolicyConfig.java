package studio.one.application.forums.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import studio.one.application.forums.domain.policy.DefaultTopicStatusPolicy;
import studio.one.application.forums.domain.policy.TopicStatusPolicy;

@Configuration
public class ForumsPolicyConfig {
    @Bean
    public TopicStatusPolicy topicStatusPolicy() {
        return new DefaultTopicStatusPolicy();
    }
}
