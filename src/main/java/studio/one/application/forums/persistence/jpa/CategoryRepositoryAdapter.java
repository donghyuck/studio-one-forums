package studio.one.application.forums.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import studio.one.application.forums.domain.model.Category;
import studio.one.application.forums.domain.repository.CategoryRepository;
import studio.one.application.forums.persistence.jpa.entity.CategoryEntity;
import studio.one.application.forums.persistence.jpa.repo.CategoryJpaRepository;

@Repository
public class CategoryRepositoryAdapter implements CategoryRepository {
    private final CategoryJpaRepository categoryJpaRepository;

    public CategoryRepositoryAdapter(CategoryJpaRepository categoryJpaRepository) {
        this.categoryJpaRepository = categoryJpaRepository;
    }

    @Override
    public Category save(Category category) {
        CategoryEntity saved = categoryJpaRepository.save(toEntity(category));
        return toDomain(saved);
    }

    @Override
    public Optional<Category> findById(Long categoryId) {
        return categoryJpaRepository.findById(categoryId).map(this::toDomain);
    }

    @Override
    public List<Category> findByForumId(Long forumId) {
        return categoryJpaRepository.findByForumId(forumId)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    private CategoryEntity toEntity(Category category) {
        return new CategoryEntity(
            category.forumId(),
            category.name(),
            category.description(),
            category.position(),
            category.createdById(),
            category.createdBy(),
            category.createdAt(),
            category.updatedById(),
            category.updatedBy(),
            category.updatedAt()
        );
    }

    private Category toDomain(CategoryEntity entity) {
        return new Category(
            entity.getId(),
            entity.getForumId(),
            entity.getName(),
            entity.getDescription(),
            entity.getPosition(),
            entity.getCreatedById(),
            entity.getCreatedBy(),
            entity.getCreatedAt(),
            entity.getUpdatedById(),
            entity.getUpdatedBy(),
            entity.getUpdatedAt()
        );
    }
}
