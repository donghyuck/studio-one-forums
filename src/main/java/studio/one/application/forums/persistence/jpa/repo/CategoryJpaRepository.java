package studio.one.application.forums.persistence.jpa.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import studio.one.application.forums.persistence.jpa.entity.CategoryEntity;

public interface CategoryJpaRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByForumId(Long forumId);
}
