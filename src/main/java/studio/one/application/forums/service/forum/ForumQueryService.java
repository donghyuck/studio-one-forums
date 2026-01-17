package studio.one.application.forums.service.forum;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import studio.one.application.forums.constant.CacheNames;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.service.forum.query.ForumDetailView;
import studio.one.application.forums.service.forum.query.ForumSummaryView;

/**
 * Forums 조회 서비스.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Service
public class ForumQueryService {
    private final ForumRepository forumRepository;

    public ForumQueryService(ForumRepository forumRepository) {
        this.forumRepository = forumRepository;
    }

    @Cacheable(cacheNames = CacheNames.Forum.BY_SLUG,
               key = "#slug",
               condition = "@environment.getProperty('studio.features.forums.cache.enabled','true') == 'true'",
               unless = "#result == null")
    public ForumDetailView getForum(String slug) {
        Forum forum = forumRepository.findBySlug(ForumSlug.of(slug))
            .orElseThrow(() -> ForumNotFoundException.bySlug(slug));
        return new ForumDetailView(
            forum.id(),
            forum.slug().value(),
            forum.name(),
            forum.description(),
            forum.updatedAt(),
            forum.version()
        );
    }

    @Cacheable(cacheNames = CacheNames.Forum.LIST,
               key = "new org.springframework.cache.interceptor.SimpleKey(#query, #inFields, #pageable)",
               condition = "@environment.getProperty('studio.features.forums.cache.enabled','true') == 'true'")
    public Page<ForumSummaryView> listForums(String query, Set<String> inFields, Pageable pageable) {
        Page<Forum> page = forumRepository.search(query, normalizeInFields(inFields), pageable);
        return page.map(forum -> new ForumSummaryView(forum.slug().value(), forum.name(), forum.updatedAt()));
    }

    public List<Forum> listForumCandidates(String query, Set<String> inFields, boolean isAdmin,
                                           boolean isMember, boolean secretListVisible, Long userId) {
        return forumRepository.searchCandidates(query, normalizeInFields(inFields),
            isAdmin, isMember, secretListVisible, userId);
    }

    private Set<String> normalizeInFields(Set<String> inFields) {
        if (inFields == null || inFields.isEmpty()) {
            return Set.of("slug", "name", "description");
        }
        return inFields.stream()
            .filter(field -> field.equals("slug") || field.equals("name") || field.equals("description"))
            .collect(Collectors.toUnmodifiableSet());
    }
}
