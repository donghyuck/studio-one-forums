package studio.one.application.forums.service.topic;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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

    public TopicDetailView getTopic(String forumSlug, Long topicId) {
        Forum forum = forumRepository.findBySlug(ForumSlug.of(forumSlug))
            .orElseThrow(() -> ForumNotFoundException.bySlug(forumSlug));
        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> TopicNotFoundException.byId(topicId));
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

    public List<TopicSummaryView> listTopics(String forumSlug, String query, Set<String> inFields,
                                             Set<String> fields, Pageable pageable) {
        Forum forum = forumRepository.findBySlug(ForumSlug.of(forumSlug))
            .orElseThrow(() -> ForumNotFoundException.bySlug(forumSlug));
        return topicQueryRepository.findTopics(forum.id(), query, inFields, fields, pageable)
            .stream()
            .map(row -> new TopicSummaryView(row.getTopicId(), row.getTitle(), row.getStatus(), row.getUpdatedAt()))
            .collect(Collectors.toList());
    }
}
