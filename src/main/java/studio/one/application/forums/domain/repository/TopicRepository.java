package studio.one.application.forums.domain.repository;

import java.util.Optional;
import studio.one.application.forums.domain.model.Topic;

public interface TopicRepository {
    Topic save(Topic topic);

    Optional<Topic> findById(Long topicId);
}
