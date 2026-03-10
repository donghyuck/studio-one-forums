package studio.one.application.forums.service.post;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import studio.one.application.forums.persistence.jdbc.PostQueryRepository;
import studio.one.application.forums.service.post.query.PostSummaryView;
import studio.one.application.forums.service.support.ForumResourceGuard;

/**
 * Forums 조회 서비스.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Service
public class PostQueryService {
    private final PostQueryRepository postQueryRepository;
    private final ForumResourceGuard forumResourceGuard;

    public PostQueryService(ForumResourceGuard forumResourceGuard, PostQueryRepository postQueryRepository) {
        this.forumResourceGuard = forumResourceGuard;
        this.postQueryRepository = postQueryRepository;
    }

    @Cacheable(cacheResolver = "topicPostListCacheResolver",
               key = "new org.springframework.cache.interceptor.SimpleKey(#pageable)",
               condition = "@environment.getProperty('studio.features.forums.cache.enabled','true') == 'true'")
    public List<PostSummaryView> listPosts(String forumSlug, Long topicId, Pageable pageable,
                                           boolean includeDeleted, boolean includeHidden) {
        var topic = forumResourceGuard.requireTopicInForum(forumSlug, topicId);
        return postQueryRepository.findPosts(topic.id(), pageable, includeDeleted, includeHidden)
            .stream()
            .map(row -> new PostSummaryView(
                row.getPostId(),
                row.getContent(),
                row.getCreatedById(),
                row.getCreatedBy(),
                row.getCreatedAt(),
                row.getVersion()
            ))
            .collect(Collectors.toList());
    }
}
