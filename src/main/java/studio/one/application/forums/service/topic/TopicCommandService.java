package studio.one.application.forums.service.topic;

import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.one.application.forums.domain.exception.CategoryForumMismatchException;
import studio.one.application.forums.domain.exception.CategoryNotFoundException;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.exception.TopicNotFoundException;
import studio.one.application.forums.domain.exception.TopicStatusTransitionNotAllowedException;
import studio.one.application.forums.domain.exception.TopicVersionMismatchException;
import studio.one.application.forums.domain.model.Category;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.model.Topic;
import studio.one.application.forums.domain.policy.TopicStatusPolicy;
import studio.one.application.forums.domain.repository.CategoryRepository;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.domain.type.TopicStatus;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.service.topic.command.ChangeTopicStatusCommand;
import studio.one.application.forums.service.topic.command.CreateTopicCommand;

@Service
public class TopicCommandService {
    private final ForumRepository forumRepository;
    private final CategoryRepository categoryRepository;
    private final TopicRepository topicRepository;
    private final TopicStatusPolicy topicStatusPolicy;

    public TopicCommandService(ForumRepository forumRepository,
                               CategoryRepository categoryRepository,
                               TopicRepository topicRepository,
                               TopicStatusPolicy topicStatusPolicy) {
        this.forumRepository = forumRepository;
        this.categoryRepository = categoryRepository;
        this.topicRepository = topicRepository;
        this.topicStatusPolicy = topicStatusPolicy;
    }

    @Transactional
    public Topic createTopic(CreateTopicCommand command) {
        Forum forum = forumRepository.findBySlug(ForumSlug.of(command.forumSlug()))
            .orElseThrow(() -> ForumNotFoundException.bySlug(command.forumSlug()));
        Category category = categoryRepository.findById(command.categoryId())
            .orElseThrow(() -> CategoryNotFoundException.byId(command.categoryId()));
        if (!category.forumId().equals(forum.id())) {
            throw CategoryForumMismatchException.of(category.id(), forum.id());
        }
        OffsetDateTime now = OffsetDateTime.now();
        Topic topic = new Topic(
            null,
            forum.id(),
            category.id(),
            command.title(),
            command.tags(),
            TopicStatus.OPEN,
            command.createdById(),
            command.createdBy(),
            now,
            command.createdById(),
            command.createdBy(),
            now,
            0L
        );
        return topicRepository.save(topic);
    }

    @Transactional
    public Topic changeStatus(ChangeTopicStatusCommand command) {
        Topic topic = topicRepository.findById(command.topicId())
            .orElseThrow(() -> TopicNotFoundException.byId(command.topicId()));
        if (topic.version() != command.expectedVersion()) {
            throw TopicVersionMismatchException.byId(command.topicId());
        }
        if (!topicStatusPolicy.canTransition(topic.status(), command.status())) {
            throw TopicStatusTransitionNotAllowedException.of(topic.status().name(), command.status().name());
        }
        topic.changeStatus(command.status(), command.updatedById(), command.updatedBy(), OffsetDateTime.now());
        return topicRepository.save(topic);
    }
}
