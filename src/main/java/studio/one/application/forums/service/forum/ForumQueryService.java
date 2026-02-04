package studio.one.application.forums.service.forum;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import studio.one.application.forums.constant.CacheNames;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.property.ForumProperties;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.persistence.jdbc.ForumQueryRepository;
import studio.one.application.forums.persistence.jdbc.ForumSummaryMetricsRow;
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
    private final ForumQueryRepository forumQueryRepository;

    public ForumQueryService(ForumRepository forumRepository, ForumQueryRepository forumQueryRepository) {
        this.forumRepository = forumRepository;
        this.forumQueryRepository = forumQueryRepository;
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
            forum.type() != null ? forum.type() : ForumType.COMMON,
            ForumProperties.readViewType(forum.properties()),
            forum.properties(),
            forum.updatedAt(),
            forum.version()
        );
    }

    @Cacheable(cacheNames = CacheNames.Forum.LIST,
               key = "new org.springframework.cache.interceptor.SimpleKey(#query, #inFields, #pageable)",
               condition = "@environment.getProperty('studio.features.forums.cache.enabled','true') == 'true'")
    public Page<ForumSummaryView> listForums(String query, Set<String> inFields, Pageable pageable) {
        return listForums(query, inFields, pageable, false);
    }

    public Page<ForumSummaryView> listForums(String query, Set<String> inFields, Pageable pageable,
                                             boolean includeHiddenPosts) {
        Page<Forum> page = forumRepository.search(query, normalizeInFields(inFields), pageable);
        Map<Long, ForumSummaryMetricsRow> metrics = forumQueryRepository.findForumSummaries(
            page.getContent().stream().map(Forum::id).toList(),
            includeHiddenPosts
        );
        return page.map(forum -> toSummaryView(forum, metrics.get(forum.id())));
    }

    public List<Forum> listForumCandidates(String query, Set<String> inFields, boolean isAdmin,
                                           boolean isMember, boolean secretListVisible, Long userId) {
        return forumRepository.searchCandidates(query, normalizeInFields(inFields),
            isAdmin, isMember, secretListVisible, userId);
    }

    public Page<Forum> listForumCandidatesPage(String query, Set<String> inFields, boolean isAdmin,
                                               boolean isMember, boolean secretListVisible, Long userId, Pageable pageable) {
        return forumRepository.searchCandidatesPage(query, normalizeInFields(inFields),
            isAdmin, isMember, secretListVisible, userId, pageable);
    }

    public List<ForumSummaryView> summarizeForums(List<Forum> forums) {
        return summarizeForums(forums, false);
    }

    public List<ForumSummaryView> summarizeForums(List<Forum> forums, boolean includeHiddenPosts) {
        if (forums == null || forums.isEmpty()) {
            return List.of();
        }
        Map<Long, ForumSummaryMetricsRow> metrics = forumQueryRepository.findForumSummaries(
            forums.stream().map(Forum::id).toList(),
            includeHiddenPosts
        );
        return forums.stream()
            .map(forum -> toSummaryView(forum, metrics.get(forum.id())))
            .toList();
    }

    private ForumSummaryView toSummaryView(Forum forum, ForumSummaryMetricsRow metrics) {
        long topicCount = metrics != null ? metrics.getTopicCount() : 0L;
        long postCount = metrics != null ? metrics.getPostCount() : 0L;
        java.time.OffsetDateTime lastActivityAt = metrics != null ? metrics.getLastActivityAt() : null;
        Long lastActivityById = metrics != null ? metrics.getLastActivityById() : null;
        String lastActivityBy = metrics != null ? metrics.getLastActivityBy() : null;
        String lastActivityType = metrics != null ? metrics.getLastActivityType() : null;
        Long lastActivityId = metrics != null ? metrics.getLastActivityId() : null;
        return new ForumSummaryView(
            forum.slug().value(),
            forum.name(),
            forum.type() != null ? forum.type() : ForumType.COMMON,
            ForumProperties.readViewType(forum.properties()),
            forum.updatedAt(),
            topicCount,
            postCount,
            lastActivityAt,
            lastActivityById,
            lastActivityBy,
            lastActivityType,
            lastActivityId
        );
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
