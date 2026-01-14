package studio.one.application.forums.service.category;

import java.time.OffsetDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.one.application.forums.domain.event.CategoryCreatedEvent;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.model.Category;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.CategoryRepository;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.service.category.command.CreateCategoryCommand;

/**
 * Forums 명령 서비스.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Service
public class CategoryCommandService {
    private final ForumRepository forumRepository;
    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CategoryCommandService(ForumRepository forumRepository,
                                  CategoryRepository categoryRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this.forumRepository = forumRepository;
        this.categoryRepository = categoryRepository;
        this.eventPublisher = eventPublisher;
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
        Category saved = categoryRepository.save(category);
        eventPublisher.publishEvent(new CategoryCreatedEvent(command.forumSlug(), null, now));
        return saved;
    }
}
