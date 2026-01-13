package studio.one.application.forums.domain.repository;

import java.util.List;
import java.util.Optional;
import studio.one.application.forums.domain.model.Category;

public interface CategoryRepository {
    Category save(Category category);

    Optional<Category> findById(Long categoryId);

    List<Category> findByForumId(Long forumId);
}
