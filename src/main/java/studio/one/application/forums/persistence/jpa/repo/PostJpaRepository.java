package studio.one.application.forums.persistence.jpa.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import studio.one.application.forums.persistence.jpa.entity.PostEntity;

public interface PostJpaRepository extends JpaRepository<PostEntity, Long> {
    List<PostEntity> findByTopicId(Long topicId);
}
