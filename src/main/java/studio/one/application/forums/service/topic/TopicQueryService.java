package studio.one.application.forums.service.topic;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import studio.one.application.forums.constant.CacheNames;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.exception.TopicNotFoundException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.model.Topic;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.persistence.jdbc.TopicListRow;
import studio.one.application.forums.persistence.jdbc.TopicQueryRepository;
import studio.one.application.forums.service.topic.query.TopicDetailView;
import studio.one.application.forums.service.topic.query.TopicSummaryView;

/**
 * Forums 조회 서비스.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Service
public class TopicQueryService {
    private final ForumRepository forumRepository;
    private final TopicRepository topicRepository;
    private final TopicQueryRepository topicQueryRepository;

    public TopicQueryService(ForumRepository forumRepository,
                             TopicRepository topicRepository,
                             TopicQueryRepository topicQueryRepository) {
        this.forumRepository = forumRepository;
        this.topicRepository = topicRepository;
        this.topicQueryRepository = topicQueryRepository;
    }

    @Cacheable(cacheNames = CacheNames.Topic.BY_ID,
               key = "new org.springframework.cache.interceptor.SimpleKey(#forumSlug, #topicId)",
               condition = "@environment.getProperty('studio.features.forums.cache.enabled','true') == 'true'",
               unless = "#result == null")
    public TopicDetailView getTopic(String forumSlug, Long topicId) {
        Forum forum = forumRepository.findBySlug(ForumSlug.of(forumSlug))
            .orElseThrow(() -> ForumNotFoundException.bySlug(forumSlug));
        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> TopicNotFoundException.byId(topicId));
        if (topic.deletedAt() != null) {
            throw TopicNotFoundException.byId(topicId);
        }
        if (!topic.forumId().equals(forum.id())) {
            throw TopicNotFoundException.inForum(topicId, forum.id());
        }
        return new TopicDetailView(
            topic.id(),
            topic.categoryId(),
            topic.title(),
            topic.tags(),
            topic.status().name(),
            topic.updatedAt(),
            topic.version()
        );
    }

    @Cacheable(cacheResolver = "forumTopicListCacheResolver",
               key = "new org.springframework.cache.interceptor.SimpleKey(#query, #inFields, #fields, #pageable)",
               condition = "@environment.getProperty('studio.features.forums.cache.enabled','true') == 'true'")
    public List<TopicSummaryView> listTopics(String forumSlug, String query, Set<String> inFields,
                                             Set<String> fields, Pageable pageable, boolean includeDeleted) {
        Forum forum = forumRepository.findBySlug(ForumSlug.of(forumSlug))
            .orElseThrow(() -> ForumNotFoundException.bySlug(forumSlug));
        return topicQueryRepository.findTopics(forum.id(), query, inFields, fields, pageable, includeDeleted)
            .stream()
            .map(row -> new TopicSummaryView(row.getTopicId(), row.getTitle(), row.getStatus(), row.getUpdatedAt()))
            .collect(Collectors.toList());
    }
}
