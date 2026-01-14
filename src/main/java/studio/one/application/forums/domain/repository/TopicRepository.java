package studio.one.application.forums.domain.repository;

import java.util.Optional;
import studio.one.application.forums.domain.model.Topic;

/**
 * Forums 도메인 저장소 인터페이스.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public interface TopicRepository {
    Topic save(Topic topic);

    Optional<Topic> findById(Long topicId);
}
