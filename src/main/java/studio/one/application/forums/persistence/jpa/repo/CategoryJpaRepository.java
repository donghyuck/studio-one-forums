package studio.one.application.forums.persistence.jpa.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import studio.one.application.forums.persistence.jpa.entity.CategoryEntity;

/**
 * Forums JPA 리포지토리.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByForumId(Long forumId);
}
