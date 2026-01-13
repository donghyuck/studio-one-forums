package studio.one.application.forums.service.category;

import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.model.Category;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.CategoryRepository;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.service.category.command.CreateCategoryCommand;

@Service
public class CategoryCommandService {
    private final ForumRepository forumRepository;
    private final CategoryRepository categoryRepository;

    public CategoryCommandService(ForumRepository forumRepository, CategoryRepository categoryRepository) {
        this.forumRepository = forumRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category createCategory(CreateCategoryCommand command) {
        Forum forum = forumRepository.findBySlug(ForumSlug.of(command.forumSlug()))
            .orElseThrow(() -> ForumNotFoundException.bySlug(command.forumSlug()));
        OffsetDateTime now = OffsetDateTime.now();
        Category category = new Category(
            null,
            forum.id(),
            command.name(),
            command.description(),
            command.position(),
            command.createdById(),
            command.createdBy(),
            now,
            command.createdById(),
            command.createdBy(),
            now
        );
        return categoryRepository.save(category);
    }
}
