package studio.one.application.forums.service.post;

import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import studio.one.application.forums.domain.event.PostCreatedEvent;
import studio.one.application.forums.domain.exception.PostNotFoundException;
import studio.one.application.forums.domain.exception.PostVersionMismatchException;
import studio.one.application.forums.domain.model.Post;
import studio.one.application.forums.domain.model.Topic;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.service.audit.ForumAuditLogService;
import studio.one.application.forums.service.post.command.CreatePostCommand;
import studio.one.application.forums.service.post.command.DeletePostCommand;
import studio.one.application.forums.service.post.command.HidePostCommand;
import studio.one.application.forums.service.post.command.UpdatePostCommand;
import studio.one.application.forums.service.support.ForumResourceGuard;

/**
 * Forums 명령 서비스.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Service
@Slf4j
public class PostCommandService {
    private final ForumResourceGuard forumResourceGuard;
    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ForumAuditLogService auditLogService;
    private final ForumPostAttachmentService postAttachmentService;

    public PostCommandService(ForumResourceGuard forumResourceGuard,
                              PostRepository postRepository,
                              ApplicationEventPublisher eventPublisher,
                              ForumAuditLogService auditLogService,
                              ForumPostAttachmentService postAttachmentService) {
        this.forumResourceGuard = forumResourceGuard;
        this.postRepository = postRepository;
        this.eventPublisher = eventPublisher;
        this.auditLogService = auditLogService;
        this.postAttachmentService = postAttachmentService;
    }

    @Transactional
    public Post createPost(CreatePostCommand command) {
        Topic topic = forumResourceGuard.requireTopicInForum(command.forumSlug(), command.topicId());
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
        log.info("post created: forumSlug={}, topicId={}, postId={}", forumSlug, topic.id(), saved.id());
        return saved;
    }

    @Transactional
    public Post hidePost(HidePostCommand command) {
        Post post = forumResourceGuard.requirePostInTopic(command.forumSlug(), command.topicId(), command.postId());
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
        Topic topic = forumResourceGuard.requireTopicInForum(command.forumSlug(), command.topicId());
        auditLogService.record(topic.forumId(), "POST", saved.id(), "HIDE", command.updatedById(),
            command.reason() == null ? null : Map.of("reason", command.reason()));
        log.info("post hidden state changed: forumSlug={}, topicId={}, postId={}, hidden={}",
            command.forumSlug(), command.topicId(), saved.id(), command.hidden());
        return saved;
    }

    @Transactional
    public Post updatePost(UpdatePostCommand command) {
        Post post = forumResourceGuard.requirePostInTopic(command.forumSlug(), command.topicId(), command.postId());
        if (post.deletedAt() != null) {
            throw PostNotFoundException.byId(command.postId());
        }
        if (post.version() != command.expectedVersion()) {
            throw PostVersionMismatchException.byId(command.postId());
        }
        OffsetDateTime now = OffsetDateTime.now();
        post.updateContent(command.content(), command.updatedById(), command.updatedBy(), now);
        Post saved = postRepository.save(post);
        log.info("post updated: forumSlug={}, topicId={}, postId={}",
            command.forumSlug(), command.topicId(), saved.id());
        return saved;
    }

    @Transactional
    public Post deletePost(DeletePostCommand command) {
        Post post = forumResourceGuard.requirePostInTopic(command.forumSlug(), command.topicId(), command.postId());
        if (post.version() != command.expectedVersion()) {
            throw PostVersionMismatchException.byId(command.postId());
        }
        OffsetDateTime now = OffsetDateTime.now();
        post.softDelete(command.deletedById(), now);
        post.touchUpdated(command.deletedById(), command.deletedBy(), now);
        Post saved = postRepository.save(post);
        Topic topic = forumResourceGuard.requireTopicInForum(command.forumSlug(), command.topicId());
        auditLogService.record(topic.forumId(), "POST", saved.id(), "DELETE", command.deletedById(), null);
        postAttachmentService.deleteAll(command.forumSlug(), command.topicId(), command.postId());
        log.info("post deleted: forumSlug={}, topicId={}, postId={}",
            command.forumSlug(), command.topicId(), saved.id());
        return saved;
    }

    private String requireForumSlug(String forumSlug) {
        if (forumSlug == null || forumSlug.isBlank()) {
            throw new IllegalArgumentException("forumSlug is required");
        }
        return forumSlug;
    }
}
