package studio.one.application.forums.persistence.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import studio.one.application.forums.persistence.jpa.entity.TopicEntity;

public interface TopicJpaRepository extends JpaRepository<TopicEntity, Long> {
}
