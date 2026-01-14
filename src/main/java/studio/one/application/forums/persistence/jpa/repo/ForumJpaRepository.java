package studio.one.application.forums.persistence.jpa.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import studio.one.application.forums.persistence.jpa.entity.ForumEntity;

/**
 * Forums JPA 리포지토리.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public interface ForumJpaRepository extends JpaRepository<ForumEntity, Long> {
    Optional<ForumEntity> findBySlug(String slug);
}
