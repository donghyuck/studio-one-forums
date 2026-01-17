package studio.one.application.forums.service.category;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import studio.one.application.forums.constant.CacheNames;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.CategoryRepository;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.service.category.query.CategorySummaryView;

/**
 * Forums 조회 서비스.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Service
public class CategoryQueryService {
    private final ForumRepository forumRepository;
    private final CategoryRepository categoryRepository;

    public CategoryQueryService(ForumRepository forumRepository, CategoryRepository categoryRepository) {
        this.forumRepository = forumRepository;
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(cacheNames = CacheNames.Category.BY_FORUM,
               key = "#forumSlug",
               condition = "@environment.getProperty('studio.features.forums.cache.enabled','true') == 'true'")
    public List<CategorySummaryView> listCategories(String forumSlug) {
        Forum forum = forumRepository.findBySlug(ForumSlug.of(forumSlug))
            .orElseThrow(() -> ForumNotFoundException.bySlug(forumSlug));
        return categoryRepository.findByForumId(forum.id())
            .stream()
            .map(category -> new CategorySummaryView(
                category.id(),
                category.name(),
                category.description(),
                category.position()
            ))
            .collect(Collectors.toList());
    }
}
