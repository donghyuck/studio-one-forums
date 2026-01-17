package studio.one.application.forums.service.post;

import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.one.application.forums.domain.event.PostCreatedEvent;
import studio.one.application.forums.domain.exception.PostNotFoundException;
import studio.one.application.forums.domain.exception.PostVersionMismatchException;
import studio.one.application.forums.domain.exception.TopicNotFoundException;
import studio.one.application.forums.domain.model.Post;
import studio.one.application.forums.domain.model.Topic;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.service.audit.ForumAuditLogService;
import studio.one.application.forums.service.post.command.CreatePostCommand;
import studio.one.application.forums.service.post.command.DeletePostCommand;
import studio.one.application.forums.service.post.command.HidePostCommand;

/**
 * Forums 명령 서비스.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Service
public class PostCommandService {
    private final TopicRepository topicRepository;
    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ForumAuditLogService auditLogService;

    public PostCommandService(TopicRepository topicRepository,
                              PostRepository postRepository,
                              ApplicationEventPublisher eventPublisher,
                              ForumAuditLogService auditLogService) {
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
        this.eventPublisher = eventPublisher;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Post createPost(CreatePostCommand command) {
        Topic topic = topicRepository.findById(command.topicId())
            .orElseThrow(() -> TopicNotFoundException.byId(command.topicId()));
        OffsetDateTime now = OffsetDateTime.now();
        Post post = new Post(
            null,
            topic.id(),
            command.content(),
            command.createdById(),
            command.createdBy(),
            now,
            command.createdById(),
            command.createdBy(),
            now,
            null,
            null,
            null,
            null,
            0L
        );
        Post saved = postRepository.save(post);
        String forumSlug = requireForumSlug(command.forumSlug());
        eventPublisher.publishEvent(new PostCreatedEvent(forumSlug, saved.topicId(), now));
        return saved;
    }

    @Transactional
    public Post hidePost(HidePostCommand command) {
        Post post = postRepository.findById(command.postId())
            .orElseThrow(() -> PostNotFoundException.byId(command.postId()));
        if (post.version() != command.expectedVersion()) {
            throw PostVersionMismatchException.byId(command.postId());
        }
        OffsetDateTime now = OffsetDateTime.now();
        if (command.hidden()) {
            post.hide(command.updatedById(), now);
        } else {
            post.hide(null, null);
        }
        post.touchUpdated(command.updatedById(), command.updatedBy(), now);
        Post saved = postRepository.save(post);
        Topic topic = topicRepository.findById(saved.topicId())
            .orElseThrow(() -> TopicNotFoundException.byId(saved.topicId()));
        auditLogService.record(topic.forumId(), "POST", saved.id(), "HIDE", command.updatedById(),
            command.reason() == null ? null : Map.of("reason", command.reason()));
        return saved;
    }

    @Transactional
    public Post deletePost(DeletePostCommand command) {
        Post post = postRepository.findById(command.postId())
            .orElseThrow(() -> PostNotFoundException.byId(command.postId()));
        if (post.version() != command.expectedVersion()) {
            throw PostVersionMismatchException.byId(command.postId());
        }
        OffsetDateTime now = OffsetDateTime.now();
        post.softDelete(command.deletedById(), now);
        post.touchUpdated(command.deletedById(), command.deletedBy(), now);
        Post saved = postRepository.save(post);
        Topic topic = topicRepository.findById(saved.topicId())
            .orElseThrow(() -> TopicNotFoundException.byId(saved.topicId()));
        auditLogService.record(topic.forumId(), "POST", saved.id(), "DELETE", command.deletedById(), null);
        return saved;
    }

    private String requireForumSlug(String forumSlug) {
        if (forumSlug == null || forumSlug.isBlank()) {
            throw new IllegalArgumentException("forumSlug is required");
        }
        return forumSlug;
    }
}
