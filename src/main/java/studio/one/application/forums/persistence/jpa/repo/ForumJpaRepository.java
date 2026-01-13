package studio.one.application.forums.persistence.jpa.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import studio.one.application.forums.persistence.jpa.entity.ForumEntity;

public interface ForumJpaRepository extends JpaRepository<ForumEntity, Long> {
    Optional<ForumEntity> findBySlug(String slug);
}
