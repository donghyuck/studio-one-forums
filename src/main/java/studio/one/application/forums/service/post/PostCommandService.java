package studio.one.application.forums.service.post;

import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.one.application.forums.domain.exception.TopicNotFoundException;
import studio.one.application.forums.domain.model.Post;
import studio.one.application.forums.domain.model.Topic;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.service.post.command.CreatePostCommand;

@Service
public class PostCommandService {
    private final TopicRepository topicRepository;
    private final PostRepository postRepository;

    public PostCommandService(TopicRepository topicRepository, PostRepository postRepository) {
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
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
        return postRepository.save(post);
    }
}
