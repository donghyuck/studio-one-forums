package studio.one.application.forums.service.authz;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import studio.one.application.forums.domain.acl.Effect;
import studio.one.application.forums.domain.acl.ForumAclRule;
import studio.one.application.forums.domain.acl.IdentifierType;
import studio.one.application.forums.domain.acl.Ownership;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.acl.SubjectType;
import studio.one.application.forums.domain.model.Post;
import studio.one.application.forums.domain.model.Topic;
import studio.one.application.forums.domain.repository.ForumAclRuleRepository;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.domain.type.TopicStatus;
import studio.one.application.forums.service.authz.PolicyDecision;

class ForumAuthorizationServiceTest {

    @Test
    void allowsOwnerWhenOwnerOnlyRuleExists() {
        List<ForumAclRule> rules = List.of(rule(1L, 10L, null, "ROLE_MEMBER", PermissionAction.EDIT_POST,
            Effect.ALLOW, Ownership.OWNER_ONLY, 0));
        ForumAuthorizationService service = serviceWithRules(rules, post(100L, 200L, 55L), topic(200L, 10L, null));

        boolean allowed = service.canPost(100L, Set.of("ROLE_MEMBER"), PermissionAction.EDIT_POST, 55L);

        assertThat(allowed).isTrue();
    }

    @Test
    void deniesWhenDenyRuleExistsEvenWithAllow() {
        List<ForumAclRule> rules = List.of(
            rule(1L, 10L, null, "ROLE_MEMBER", PermissionAction.READ_TOPIC_LIST, Effect.ALLOW, Ownership.ANY, 10),
            rule(2L, 10L, null, "ROLE_MEMBER", PermissionAction.READ_TOPIC_LIST, Effect.DENY, Ownership.ANY, 0)
        );
        ForumAuthorizationService service = serviceWithRules(rules, null, null);

        boolean allowed = service.canBoard(10L, Set.of("ROLE_MEMBER"), PermissionAction.READ_TOPIC_LIST);

        assertThat(allowed).isFalse();
    }

    @Test
    void categorySpecificRuleAppliesWhenBoardRuleDoesNotMatchOwnership() {
        List<ForumAclRule> rules = List.of(
            rule(1L, 10L, null, "ROLE_MEMBER", PermissionAction.EDIT_POST, Effect.ALLOW, Ownership.NON_OWNER_ONLY, 0),
            rule(2L, 10L, 5L, "ROLE_MEMBER", PermissionAction.EDIT_POST, Effect.ALLOW, Ownership.OWNER_ONLY, 1)
        );
        ForumAuthorizationService service = serviceWithRules(rules, post(101L, 201L, 77L), topic(201L, 10L, 5L));

        boolean allowed = service.canPost(101L, Set.of("ROLE_MEMBER"), PermissionAction.EDIT_POST, 77L);

        assertThat(allowed).isTrue();
    }

    @Test
    void moderatorCanDeleteAnyPostWhenAllowAnyExists() {
        List<ForumAclRule> rules = List.of(
            rule(1L, 10L, null, "ROLE_MEMBER", PermissionAction.DELETE_POST, Effect.ALLOW, Ownership.OWNER_ONLY, 1),
            rule(2L, 10L, null, "ROLE_MODERATOR", PermissionAction.DELETE_POST, Effect.ALLOW, Ownership.ANY, 10)
        );
        ForumAuthorizationService service = serviceWithRules(rules, post(102L, 202L, 88L), topic(202L, 10L, null));

        boolean allowed = service.canPost(102L, Set.of("ROLE_MODERATOR"), PermissionAction.DELETE_POST, 999L);

        assertThat(allowed).isTrue();
    }

    @Test
    void deniesWhenNoRuleMatches() {
        ForumAuthorizationService service = serviceWithRules(List.of(), post(103L, 203L, 77L), topic(203L, 10L, null));

        boolean allowed = service.canPost(103L, Set.of("ROLE_MEMBER"), PermissionAction.EDIT_POST, 77L);

        assertThat(allowed).isFalse();
    }

    @Test
    void userSpecificRuleMatchesByUserId() {
        List<ForumAclRule> rules = List.of(
            userRule(1L, 10L, null, 55L, PermissionAction.READ_TOPIC_CONTENT, Effect.ALLOW, Ownership.ANY, 0)
        );
        ForumAuthorizationService service = serviceWithRules(rules, post(104L, 204L, 55L), topic(204L, 10L, null));

        boolean allowed = service.decideWithOwnership(10L, null, Set.of("ROLE_MEMBER"), Set.of(),
            PermissionAction.READ_TOPIC_CONTENT, 55L, 55L, null) == PolicyDecision.ALLOW;

        assertThat(allowed).isTrue();
    }

    @Test
    void userSpecificRuleMatchesByUsername() {
        List<ForumAclRule> rules = List.of(
            userNameRule(1L, 10L, null, "alice", PermissionAction.READ_TOPIC_CONTENT, Effect.ALLOW, Ownership.ANY, 0)
        );
        ForumAuthorizationService service = serviceWithRules(rules, post(106L, 206L, 55L), topic(206L, 10L, null));

        boolean allowed = service.decideWithOwnership(10L, null, Set.of("ROLE_MEMBER"), Set.of(),
            PermissionAction.READ_TOPIC_CONTENT, 55L, 55L, "alice") == PolicyDecision.ALLOW;

        assertThat(allowed).isTrue();
    }

    @Test
    void userDenyOverridesRoleAllow() {
        List<ForumAclRule> rules = List.of(
            rule(1L, 10L, null, "ROLE_MEMBER", PermissionAction.EDIT_POST, Effect.ALLOW, Ownership.ANY, 5),
            userRule(2L, 10L, null, 99L, PermissionAction.EDIT_POST, Effect.DENY, Ownership.ANY, 0)
        );
        ForumAuthorizationService service = serviceWithRules(rules, post(105L, 205L, 99L), topic(205L, 10L, null));

        boolean allowed = service.decideWithOwnership(10L, null, Set.of("ROLE_MEMBER"), Set.of(),
            PermissionAction.EDIT_POST, 99L, 99L, null) == PolicyDecision.ALLOW;

        assertThat(allowed).isFalse();
    }

    private ForumAuthorizationService serviceWithRules(List<ForumAclRule> rules, Post post, Topic topic) {
        return new ForumAuthorizationService(
            new FakeForumAclRuleRepository(rules),
            new FakeTopicRepository(topic),
            new FakePostRepository(post)
        );
    }

    private ForumAclRule rule(Long ruleId, Long boardId, Long categoryId, String role, PermissionAction action,
                              Effect effect, Ownership ownership, int priority) {
        return new ForumAclRule(
            ruleId,
            boardId,
            categoryId,
            SubjectType.ROLE,
            IdentifierType.NAME,
            null,
            role,
            role,
            action,
            effect,
            ownership,
            priority,
            true,
            null,
            null,
            null,
            null
        );
    }

    private ForumAclRule userRule(Long ruleId, Long boardId, Long categoryId, Long userId, PermissionAction action,
                                  Effect effect, Ownership ownership, int priority) {
        return new ForumAclRule(
            ruleId,
            boardId,
            categoryId,
            SubjectType.USER,
            IdentifierType.ID,
            userId,
            null,
            null,
            action,
            effect,
            ownership,
            priority,
            true,
            null,
            null,
            null,
            null
        );
    }

    private ForumAclRule userNameRule(Long ruleId, Long boardId, Long categoryId, String username,
                                      PermissionAction action, Effect effect, Ownership ownership, int priority) {
        return new ForumAclRule(
            ruleId,
            boardId,
            categoryId,
            SubjectType.USER,
            IdentifierType.NAME,
            null,
            username,
            null,
            action,
            effect,
            ownership,
            priority,
            true,
            null,
            null,
            null,
            null
        );
    }

    private Post post(Long postId, Long topicId, Long createdById) {
        if (postId == null) {
            return null;
        }
        return new Post(
            postId,
            topicId,
            "content",
            createdById,
            "user",
            OffsetDateTime.now(),
            createdById,
            "user",
            OffsetDateTime.now(),
            null,
            null,
            null,
            null,
            0L
        );
    }

    private Topic topic(Long topicId, Long forumId, Long categoryId) {
        if (topicId == null) {
            return null;
        }
        return new Topic(
            topicId,
            forumId,
            categoryId,
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
            null,
            null,
            0L
        );
    }

    private static class FakeForumAclRuleRepository implements ForumAclRuleRepository {
        private final List<ForumAclRule> rules;

        private FakeForumAclRuleRepository(List<ForumAclRule> rules) {
            this.rules = new ArrayList<>(rules);
        }

        @Override
        public List<ForumAclRule> findRules(long boardId, Long categoryId, PermissionAction action,
                                            Set<String> roleNames, Set<Long> roleIds, Long userId, String username) {
            return rules.stream()
                .filter(rule -> rule.enabled())
                .filter(rule -> rule.boardId().equals(boardId))
                .filter(rule -> rule.action() == action)
                .filter(rule -> subjectMatches(rule, roleNames, roleIds, userId, username))
                .filter(rule -> categoryId == null ? rule.categoryId() == null : (rule.categoryId() == null || rule.categoryId().equals(categoryId)))
                .toList();
        }

        private boolean subjectMatches(ForumAclRule rule, Set<String> roleNames, Set<Long> roleIds,
                                       Long userId, String username) {
            SubjectType subjectType = rule.subjectType() != null ? rule.subjectType() : SubjectType.ROLE;
            IdentifierType identifierType = rule.identifierType() != null ? rule.identifierType() : IdentifierType.NAME;
            if (subjectType == SubjectType.ROLE) {
                if (identifierType == IdentifierType.ID) {
                    return roleIds != null && rule.subjectId() != null && roleIds.contains(rule.subjectId());
                }
                String subjectName = rule.subjectName() != null ? rule.subjectName() : rule.role();
                return roleNames != null && subjectName != null && roleNames.contains(subjectName);
            }
            if (subjectType == SubjectType.USER) {
                if (identifierType == IdentifierType.ID) {
                    return userId != null && rule.subjectId() != null && rule.subjectId().equals(userId);
                }
                return username != null && rule.subjectName() != null && rule.subjectName().equals(username);
            }
            return false;
        }
    }

    private static class FakeTopicRepository implements TopicRepository {
        private final Map<Long, Topic> store;

        private FakeTopicRepository(Topic topic) {
            this.store = topic == null ? Map.of() : Map.of(topic.id(), topic);
        }

        @Override
        public Topic save(Topic topic) {
            throw new UnsupportedOperationException("Not needed for test");
        }

        @Override
        public Optional<Topic> findById(Long topicId) {
            return Optional.ofNullable(store.get(topicId));
        }
    }

    private static class FakePostRepository implements PostRepository {
        private final Map<Long, Post> store;

        private FakePostRepository(Post post) {
            this.store = post == null ? Map.of() : Map.of(post.id(), post);
        }

        @Override
        public Post save(Post post) {
            throw new UnsupportedOperationException("Not needed for test");
        }

        @Override
        public Optional<Post> findById(Long postId) {
            return Optional.ofNullable(store.get(postId));
        }

        @Override
        public List<Post> findByTopicId(Long topicId) {
            throw new UnsupportedOperationException("Not needed for test");
        }
    }
}
