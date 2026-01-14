package studio.one.application.forums.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import studio.one.application.forums.domain.policy.DefaultTopicStatusPolicy;
import studio.one.application.forums.domain.policy.TopicStatusPolicy;

/**
 * Forums 서비스 구성요소.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Configuration
public class ForumsPolicyConfig {
    @Bean
    public TopicStatusPolicy topicStatusPolicy() {
        return new DefaultTopicStatusPolicy();
    }
}
