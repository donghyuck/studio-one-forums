package studio.one.application.forums.web.authz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.exception.TopicNotFoundException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.model.Post;
import studio.one.application.forums.domain.model.Topic;
import studio.one.application.forums.domain.repository.CategoryRepository;
import studio.one.application.forums.domain.repository.ForumAclRuleRepository;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.domain.type.TopicStatus;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.service.authz.ForumAuthorizationService;

class ForumAuthzVisibilityTest {

    @Test
    void adminOnlyContentIsHiddenForNonAdmin() {
        Forum forum = new Forum(
            1L,
            ForumSlug.of("admin"),
            "admin",
            "",
            ForumType.ADMIN_ONLY,
            Map.of(),
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
            2L,
            "user",
            OffsetDateTime.now(),
            2L,
            "user",
            OffsetDateTime.now(),
            null,
            null,
            0L
        );
        ForumAuthz authz = new ForumAuthz(
            new ForumAuthorizationService(new EmptyAclRepo(), new SingleTopicRepo(topic), new EmptyPostRepo()),
            new SingleForumRepo(forum),
            new EmptyCategoryRepo(),
            new SingleTopicRepo(topic),
            new EmptyPostRepo(),
            new EmptyForumMemberRepo(),
            Set.of(),
            false
        );

        assertThatThrownBy(() -> authz.canTopic(10L, PermissionAction.READ_TOPIC_CONTENT.name()))
            .isInstanceOf(TopicNotFoundException.class);
    }

    @Test
    void moderatorRoleAppliesPerForum() {
        Forum forum = new Forum(
            1L,
            ForumSlug.of("notice"),
            "notice",
            "",
            ForumType.NOTICE,
            Map.of(),
            1L,
            "admin",
            OffsetDateTime.now(),
            1L,
            "admin",
            OffsetDateTime.now(),
            0L
        );
        ForumAuthz authz = new ForumAuthz(
            new ForumAuthorizationService(new EmptyAclRepo(), new EmptyTopicRepo(), new EmptyPostRepo()),
            new SingleForumRepo(forum),
            new EmptyCategoryRepo(),
            new EmptyTopicRepo(),
            new EmptyPostRepo(),
            new ForumScopedMemberRepo(1L, studio.one.application.forums.domain.type.ForumMemberRole.MODERATOR),
            Set.of(),
            false
        );

        boolean allowed = authz.canBoard("notice", PermissionAction.CREATE_TOPIC.name());

        assertThat(allowed).isTrue();
    }

    private static class EmptyAclRepo implements ForumAclRuleRepository {
        @Override
        public List<studio.one.application.forums.domain.acl.ForumAclRule> findRules(long boardId, Long categoryId,
                                                                                    PermissionAction action, Set<String> roleNames,
                                                                                    Set<Long> roleIds, Long userId, String username) {
            return List.of();
        }
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

    private static class EmptyPostRepo implements PostRepository {
        @Override
        public Post save(Post post) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Post> findById(Long postId) {
            return Optional.empty();
        }

        @Override
        public List<Post> findByTopicId(Long topicId) {
            return List.of();
        }
    }

    private static class EmptyCategoryRepo implements CategoryRepository {
        @Override
        public studio.one.application.forums.domain.model.Category save(studio.one.application.forums.domain.model.Category category) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<studio.one.application.forums.domain.model.Category> findById(Long categoryId) {
            return Optional.empty();
        }

        @Override
        public List<studio.one.application.forums.domain.model.Category> findByForumId(Long forumId) {
            return List.of();
        }

        @Override
        public void deleteById(Long categoryId) {
            throw new UnsupportedOperationException();
        }
    }

    private static class EmptyForumMemberRepo implements studio.one.application.forums.domain.repository.ForumMemberRepository {
        @Override
        public java.util.Optional<studio.one.application.forums.domain.type.ForumMemberRole> findRole(long forumId, long userId) {
            return java.util.Optional.empty();
        }

        @Override
        public java.util.Set<studio.one.application.forums.domain.type.ForumMemberRole> findRoles(long forumId, long userId) {
            return java.util.Set.of();
        }

        @Override
        public java.util.Map<Long, java.util.Set<studio.one.application.forums.domain.type.ForumMemberRole>> findRolesByUserId(long userId) {
            return java.util.Map.of();
        }

        @Override
        public java.util.List<studio.one.application.forums.domain.model.ForumMember> listMembers(long forumId, int page, int size) {
            return java.util.List.of();
        }

        @Override
        public void upsertMemberRole(long forumId, long userId, studio.one.application.forums.domain.type.ForumMemberRole role, Long actorId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeMember(long forumId, long userId) {
            throw new UnsupportedOperationException();
        }
    }

    private static class ForumScopedMemberRepo extends EmptyForumMemberRepo {
        private final long forumId;
        private final studio.one.application.forums.domain.type.ForumMemberRole role;

        private ForumScopedMemberRepo(long forumId, studio.one.application.forums.domain.type.ForumMemberRole role) {
            this.forumId = forumId;
            this.role = role;
        }

        @Override
        public java.util.Set<studio.one.application.forums.domain.type.ForumMemberRole> findRoles(long forumId, long userId) {
            if (this.forumId == forumId) {
                return java.util.Set.of(role);
            }
            return java.util.Set.of();
        }

        @Override
        public java.util.Map<Long, java.util.Set<studio.one.application.forums.domain.type.ForumMemberRole>> findRolesByUserId(long userId) {
            return java.util.Map.of(forumId, java.util.Set.of(role));
        }
    }

    private static class EmptyTopicRepo implements TopicRepository {
        @Override
        public Topic save(Topic topic) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Topic> findById(Long topicId) {
            return Optional.empty();
        }
    }
}
