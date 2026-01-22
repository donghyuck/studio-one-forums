package studio.one.application.forums.service.topic;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import studio.one.application.forums.domain.exception.TopicNotFoundException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.model.Topic;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.domain.type.TopicStatus;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.persistence.jdbc.TopicQueryRepository;

class TopicQueryServiceTest {

    @Test
    void deletedTopicIsNotVisible() {
        Forum forum = new Forum(
            1L,
            ForumSlug.of("general"),
            "General",
            "",
            ForumType.COMMON,
            java.util.Map.of(),
            1L,
            "admin",
            OffsetDateTime.now(),
            1L,
            "admin",
            OffsetDateTime.now(),
            0L
        );
        Topic topic = new Topic(
            10L,
            1L,
            2L,
            "title",
            List.of(),
            TopicStatus.OPEN,
            false,
            false,
            1L,
            "user",
            OffsetDateTime.now(),
            1L,
            "user",
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            1L,
            0L
        );
        TopicQueryService service = new TopicQueryService(new SingleForumRepo(forum), new SingleTopicRepo(topic), new EmptyTopicQueryRepo());

        assertThatThrownBy(() -> service.getTopic("general", 10L))
            .isInstanceOf(TopicNotFoundException.class);
    }

    private static class SingleForumRepo implements ForumRepository {
        private final Forum forum;

        private SingleForumRepo(Forum forum) {
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
            return Optional.of(forum);
        }

        @Override
        public boolean existsBySlug(ForumSlug slug) {
            return true;
        }

        @Override
        public List<Forum> findAll() {
            return List.of(forum);
        }

        @Override
        public org.springframework.data.domain.Page<Forum> search(String query, Set<String> inFields, org.springframework.data.domain.Pageable pageable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Forum> searchCandidates(String query, Set<String> inFields, boolean isAdmin,
                                            boolean isMember, boolean secretListVisible, Long userId) {
            return List.of(forum);
        }

        @Override
        public org.springframework.data.domain.Page<Forum> searchCandidatesPage(String query, Set<String> inFields,
                                                                                boolean isAdmin, boolean isMember,
                                                                                boolean secretListVisible, Long userId,
                                                                                org.springframework.data.domain.Pageable pageable) {
            return new org.springframework.data.domain.PageImpl<>(List.of(forum), pageable, 1);
        }
    }

    private static class SingleTopicRepo implements TopicRepository {
        private final Topic topic;

        private SingleTopicRepo(Topic topic) {
            this.topic = topic;
        }

        @Override
        public Topic save(Topic topic) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Topic> findById(Long topicId) {
            return Optional.ofNullable(topicId != null && topicId.equals(topic.id()) ? topic : null);
        }
    }

    private static class EmptyTopicQueryRepo implements TopicQueryRepository {
        @Override
        public List<studio.one.application.forums.persistence.jdbc.TopicListRow> findTopics(Long forumId, String query, Set<String> inFields, Set<String> fields,
                                                                                           org.springframework.data.domain.Pageable pageable, boolean includeDeleted, boolean includeHiddenPosts) {
            return List.of();
        }

        @Override
        public long countTopics(Long forumId, String query, Set<String> inFields, boolean includeDeleted) {
            return 0L;
        }
    }
}
