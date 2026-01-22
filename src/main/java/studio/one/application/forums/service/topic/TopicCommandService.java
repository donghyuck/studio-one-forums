package studio.one.application.forums.service.topic;

import java.time.OffsetDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.one.application.forums.domain.event.TopicCreatedEvent;
import studio.one.application.forums.domain.event.TopicStatusChangedEvent;
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
import studio.one.application.forums.service.audit.ForumAuditLogService;
import studio.one.application.forums.service.topic.command.ChangeTopicStatusCommand;
import studio.one.application.forums.service.topic.command.CreateTopicCommand;
import studio.one.application.forums.service.topic.command.DeleteTopicCommand;
import studio.one.application.forums.service.topic.command.LockTopicCommand;
import studio.one.application.forums.service.topic.command.PinTopicCommand;
import studio.one.application.forums.service.topic.command.UpdateTopicCommand;

/**
 * Forums 명령 서비스.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Service
public class TopicCommandService {
    private final ForumRepository forumRepository;
    private final CategoryRepository categoryRepository;
    private final TopicRepository topicRepository;
    private final TopicStatusPolicy topicStatusPolicy;
    private final ApplicationEventPublisher eventPublisher;
    private final ForumAuditLogService auditLogService;

    public TopicCommandService(ForumRepository forumRepository,
                               CategoryRepository categoryRepository,
                               TopicRepository topicRepository,
                               TopicStatusPolicy topicStatusPolicy,
                               ApplicationEventPublisher eventPublisher,
                               ForumAuditLogService auditLogService) {
        this.forumRepository = forumRepository;
        this.categoryRepository = categoryRepository;
        this.topicRepository = topicRepository;
        this.topicStatusPolicy = topicStatusPolicy;
        this.eventPublisher = eventPublisher;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Topic createTopic(CreateTopicCommand command) {
        Forum forum = forumRepository.findBySlug(ForumSlug.of(command.forumSlug()))
            .orElseThrow(() -> ForumNotFoundException.bySlug(command.forumSlug()));
        Long categoryId = null;
        if (command.categoryId() != null) {
            Category category = categoryRepository.findById(command.categoryId())
                .orElseThrow(() -> CategoryNotFoundException.byId(command.categoryId()));
            if (!category.forumId().equals(forum.id())) {
                throw CategoryForumMismatchException.of(category.id(), forum.id());
            }
            categoryId = category.id();
        }
        OffsetDateTime now = OffsetDateTime.now();
        Topic topic = new Topic(
            null,
            forum.id(),
            categoryId,
            command.title(),
            command.tags(),
            TopicStatus.OPEN,
            false,
            false,
            command.createdById(),
            command.createdBy(),
            now,
            command.createdById(),
            command.createdBy(),
            now,
            null,
            null,
            0L
        );
        Topic saved = topicRepository.save(topic);
        eventPublisher.publishEvent(new TopicCreatedEvent(command.forumSlug(), saved.id(), now));
        return saved;
    }

    @Transactional
    public Topic changeStatus(ChangeTopicStatusCommand command) {
        Topic topic = topicRepository.findById(command.topicId())
            .orElseThrow(() -> TopicNotFoundException.byId(command.topicId()));
        if (topic.deletedAt() != null) {
            throw TopicNotFoundException.byId(command.topicId());
        }
        if (topic.version() != command.expectedVersion()) {
            throw TopicVersionMismatchException.byId(command.topicId());
        }
        if (!topicStatusPolicy.canTransition(topic.status(), command.status())) {
            throw TopicStatusTransitionNotAllowedException.of(topic.status().name(), command.status().name());
        }
        OffsetDateTime now = OffsetDateTime.now();
        topic.changeStatus(command.status(), command.updatedById(), command.updatedBy(), now);
        Topic saved = topicRepository.save(topic);
        eventPublisher.publishEvent(new TopicStatusChangedEvent(command.forumSlug(), saved.id(), now));
        return saved;
    }

    @Transactional
    public Topic updateTopic(UpdateTopicCommand command) {
        Topic topic = topicRepository.findById(command.topicId())
            .orElseThrow(() -> TopicNotFoundException.byId(command.topicId()));
        if (topic.deletedAt() != null) {
            throw TopicNotFoundException.byId(command.topicId());
        }
        if (topic.version() != command.expectedVersion()) {
            throw TopicVersionMismatchException.byId(command.topicId());
        }
        OffsetDateTime now = OffsetDateTime.now();
        topic.updateContent(command.title(), command.tags(), command.updatedById(), command.updatedBy(), now);
        return topicRepository.save(topic);
    }

    @Transactional
    public Topic pinTopic(PinTopicCommand command) {
        Topic topic = topicRepository.findById(command.topicId())
            .orElseThrow(() -> TopicNotFoundException.byId(command.topicId()));
        if (topic.deletedAt() != null) {
            throw TopicNotFoundException.byId(command.topicId());
        }
        if (topic.version() != command.expectedVersion()) {
            throw TopicVersionMismatchException.byId(command.topicId());
        }
        OffsetDateTime now = OffsetDateTime.now();
        topic.setPinned(command.pinned(), command.updatedById(), command.updatedBy(), now);
        Topic saved = topicRepository.save(topic);
        auditLogService.record(saved.forumId(), "TOPIC", saved.id(), "PIN", command.updatedById(), null);
        return saved;
    }

    @Transactional
    public Topic lockTopic(LockTopicCommand command) {
        Topic topic = topicRepository.findById(command.topicId())
            .orElseThrow(() -> TopicNotFoundException.byId(command.topicId()));
        if (topic.deletedAt() != null) {
            throw TopicNotFoundException.byId(command.topicId());
        }
        if (topic.version() != command.expectedVersion()) {
            throw TopicVersionMismatchException.byId(command.topicId());
        }
        OffsetDateTime now = OffsetDateTime.now();
        topic.setLocked(command.locked(), command.updatedById(), command.updatedBy(), now);
        Topic saved = topicRepository.save(topic);
        auditLogService.record(saved.forumId(), "TOPIC", saved.id(), "LOCK", command.updatedById(), null);
        return saved;
    }

    @Transactional
    public Topic deleteTopic(DeleteTopicCommand command) {
        Topic topic = topicRepository.findById(command.topicId())
            .orElseThrow(() -> TopicNotFoundException.byId(command.topicId()));
        if (topic.version() != command.expectedVersion()) {
            throw TopicVersionMismatchException.byId(command.topicId());
        }
        OffsetDateTime now = OffsetDateTime.now();
        topic.softDelete(command.deletedById(), now);
        topic.setLocked(true, command.deletedById(), command.deletedBy(), now);
        Topic saved = topicRepository.save(topic);
        auditLogService.record(saved.forumId(), "TOPIC", saved.id(), "DELETE", command.deletedById(), null);
        return saved;
    }
}
