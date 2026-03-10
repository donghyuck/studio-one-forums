package studio.one.application.forums.service.support;

import org.springframework.stereotype.Component;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.exception.PostNotFoundException;
import studio.one.application.forums.domain.exception.TopicNotFoundException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.model.Post;
import studio.one.application.forums.domain.model.Topic;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.domain.vo.ForumSlug;

@Component
public class ForumResourceGuard {
    private final ForumRepository forumRepository;
    private final TopicRepository topicRepository;
    private final PostRepository postRepository;

    public ForumResourceGuard(ForumRepository forumRepository,
                              TopicRepository topicRepository,
                              PostRepository postRepository) {
        this.forumRepository = forumRepository;
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
    }

    public Forum requireForum(String forumSlug) {
        return forumRepository.findBySlug(ForumSlug.of(forumSlug))
            .orElseThrow(() -> ForumNotFoundException.bySlug(forumSlug));
    }

    public Topic requireTopicInForum(String forumSlug, Long topicId) {
        Forum forum = requireForum(forumSlug);
        return requireTopicInForum(forum.id(), topicId);
    }

    public Topic requireTopicInForum(Long forumId, Long topicId) {
        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> TopicNotFoundException.byId(topicId));
        if (!topic.forumId().equals(forumId) || topic.deletedAt() != null) {
            throw TopicNotFoundException.inForum(topicId, forumId);
        }
        return topic;
    }

    public Post requirePostInTopic(String forumSlug, Long topicId, Long postId) {
        Topic topic = requireTopicInForum(forumSlug, topicId);
        return requirePostInTopic(topic.id(), postId);
    }

    public Post requirePostInTopic(Long topicId, Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> PostNotFoundException.byId(postId));
        if (!post.topicId().equals(topicId) || post.deletedAt() != null) {
            throw PostNotFoundException.byId(postId);
        }
        return post;
    }
}
