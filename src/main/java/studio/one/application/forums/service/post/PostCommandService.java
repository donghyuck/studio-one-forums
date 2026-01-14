package studio.one.application.forums.service.post;

import java.time.OffsetDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.one.application.forums.domain.event.PostCreatedEvent;
import studio.one.application.forums.domain.exception.TopicNotFoundException;
import studio.one.application.forums.domain.model.Post;
import studio.one.application.forums.domain.model.Topic;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.service.post.command.CreatePostCommand;

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

    public PostCommandService(TopicRepository topicRepository,
                              PostRepository postRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
        this.eventPublisher = eventPublisher;
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
            now
        );
        Post saved = postRepository.save(post);
        String forumSlug = requireForumSlug(command.forumSlug());
        eventPublisher.publishEvent(new PostCreatedEvent(forumSlug, saved.topicId(), now));
        return saved;
    }

    private String requireForumSlug(String forumSlug) {
        if (forumSlug == null || forumSlug.isBlank()) {
            throw new IllegalArgumentException("forumSlug is required");
        }
        return forumSlug;
    }
}
