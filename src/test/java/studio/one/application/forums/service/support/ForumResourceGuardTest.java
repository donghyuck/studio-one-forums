package studio.one.application.forums.service.support;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import studio.one.application.forums.domain.exception.PostNotFoundException;
import studio.one.application.forums.domain.exception.TopicNotFoundException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.model.Post;
import studio.one.application.forums.domain.model.Topic;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.domain.type.TopicStatus;
import studio.one.application.forums.domain.vo.ForumSlug;

class ForumResourceGuardTest {

    @Test
    void rejectsTopicWhenForumSlugDoesNotMatch() {
        ForumResourceGuard guard = new ForumResourceGuard(
            new SingleForumRepository(forum(1L, "general")),
            new SingleTopicRepository(topic(10L, 2L)),
            new SinglePostRepository(post(100L, 10L))
        );

        assertThatThrownBy(() -> guard.requireTopicInForum("general", 10L))
            .isInstanceOf(TopicNotFoundException.class);
    }

    @Test
    void rejectsPostWhenTopicDoesNotMatch() {
        ForumResourceGuard guard = new ForumResourceGuard(
            new SingleForumRepository(forum(1L, "general")),
            new SingleTopicRepository(topic(10L, 1L)),
            new SinglePostRepository(post(100L, 20L))
        );

        assertThatThrownBy(() -> guard.requirePostInTopic("general", 10L, 100L))
            .isInstanceOf(PostNotFoundException.class);
    }

    private Forum forum(Long id, String slug) {
        return new Forum(id, ForumSlug.of(slug), slug, "", ForumType.COMMON, Map.of(),
            1L, "admin", OffsetDateTime.now(), 1L, "admin", OffsetDateTime.now(), 0L);
    }

    private Topic topic(Long id, Long forumId) {
        return new Topic(id, forumId, null, "topic", List.of(), TopicStatus.OPEN, false, false,
            1L, "user", OffsetDateTime.now(), 1L, "user", OffsetDateTime.now(), null, null, 0L);
    }

    private Post post(Long id, Long topicId) {
        return new Post(id, topicId, "content", 1L, "user", OffsetDateTime.now(), 1L, "user",
            OffsetDateTime.now(), null, null, null, null, 0L);
    }

    private static class SingleForumRepository implements ForumRepository {
        private final Forum forum;

        private SingleForumRepository(Forum forum) {
            this.forum = forum;
        }

        @Override
        public Forum save(Forum forum) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Forum> findById(Long forumId) {
            return Optional.ofNullable(forumId != null && forumId.equals(forum.id()) ? forum : null);
        }

        @Override
        public Optional<Forum> findBySlug(ForumSlug slug) {
            return Optional.ofNullable(forum.slug().equals(slug) ? forum : null);
        }

        @Override
        public boolean existsBySlug(ForumSlug slug) {
            return forum.slug().equals(slug);
        }

        @Override
        public List<Forum> findAll() {
            return List.of(forum);
        }

        @Override
        public org.springframework.data.domain.Page<Forum> search(String query, Set<String> inFields,
                                                                  org.springframework.data.domain.Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Forum> searchCandidates(String query, Set<String> inFields, boolean isAdmin,
                                            boolean isMember, boolean secretListVisible, Long userId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.springframework.data.domain.Page<Forum> searchCandidatesPage(String query, Set<String> inFields,
                                                                                boolean isAdmin, boolean isMember,
                                                                                boolean secretListVisible, Long userId,
                                                                                org.springframework.data.domain.Pageable pageable) {
            throw new UnsupportedOperationException();
        }
    }

    private static class SingleTopicRepository implements TopicRepository {
        private final Topic topic;

        private SingleTopicRepository(Topic topic) {
            this.topic = topic;
        }

        @Override
        public Topic save(Topic topic) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Topic> findById(Long topicId) {
            return Optional.ofNullable(this.topic.id().equals(topicId) ? this.topic : null);
        }
    }

    private static class SinglePostRepository implements PostRepository {
        private final Post post;

        private SinglePostRepository(Post post) {
            this.post = post;
        }

        @Override
        public Post save(Post post) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Post> findById(Long postId) {
            return Optional.ofNullable(this.post.id().equals(postId) ? this.post : null);
        }

        @Override
        public List<Post> findByTopicId(Long topicId) {
            return List.of();
        }
    }
}
