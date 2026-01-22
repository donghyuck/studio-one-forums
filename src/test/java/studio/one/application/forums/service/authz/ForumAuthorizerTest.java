package studio.one.application.forums.service.authz;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.ForumMemberRepository;
import studio.one.application.forums.domain.type.ForumMemberRole;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.service.authz.policy.AdminOnlyBoardTypePolicy;
import studio.one.application.forums.service.authz.policy.CommonBoardTypePolicy;
import studio.one.application.forums.service.authz.policy.NoticeBoardTypePolicy;
import studio.one.application.forums.service.authz.policy.SecretBoardTypePolicy;

class ForumAuthorizerTest {
    @Test
    void deniesWhenPolicyDenies() {
        ForumAuthorizer authorizer = authorizer(PolicyDecision.DENY, PolicyDecision.ABSTAIN);
        AuthorizationDecision decision = authorizer.authorize(forum(), null, PermissionAction.READ_BOARD, null,
            false, new ForumAccessContext(Set.of("MEMBER"), 1L, "user"), Set.of("MEMBER"), true);

        assertThat(decision.isAllowed()).isFalse();
        assertThat(decision.getDenyReason()).isEqualTo(DenyReason.POLICY_DENY);
    }

    @Test
    void deniesWhenAclDenies() {
        ForumAuthorizer authorizer = authorizer(PolicyDecision.ALLOW, PolicyDecision.DENY);
        AuthorizationDecision decision = authorizer.authorize(forum(), null, PermissionAction.READ_BOARD, null,
            false, new ForumAccessContext(Set.of("MEMBER"), 1L, "user"), Set.of("MEMBER"), true);

        assertThat(decision.isAllowed()).isFalse();
        assertThat(decision.getDenyReason()).isEqualTo(DenyReason.ACL_DENY);
    }

    @Test
    void allowsWhenAclAllows() {
        ForumAuthorizer authorizer = authorizer(PolicyDecision.ABSTAIN, PolicyDecision.ALLOW);
        AuthorizationDecision decision = authorizer.authorize(forum(), null, PermissionAction.READ_BOARD, null,
            false, new ForumAccessContext(Set.of("MEMBER"), 1L, "user"), Set.of("MEMBER"), true);

        assertThat(decision.isAllowed()).isTrue();
    }

    @Test
    void deniesWhenPolicyAbstainsAndNoAcl() {
        ForumAuthorizer authorizer = authorizer(PolicyDecision.ABSTAIN, PolicyDecision.ABSTAIN);
        AuthorizationDecision decision = authorizer.authorize(forum(), null, PermissionAction.READ_BOARD, null,
            false, new ForumAccessContext(Set.of("MEMBER"), 1L, "user"), Set.of("MEMBER"), true);

        assertThat(decision.isAllowed()).isFalse();
        assertThat(decision.getDenyReason()).isEqualTo(DenyReason.POLICY_ABSTAIN);
    }

    private ForumAuthorizer authorizer(PolicyDecision policyDecision, PolicyDecision aclDecision) {
        ForumAccessResolver accessResolver = new ForumAccessResolver(new EmptyForumMemberRepo(), Set.of("ADMIN"));
        ForumPolicyEngine policyEngine = new StubPolicyEngine(policyDecision);
        ForumAclAuthorizer aclAuthorizer = new StubAclAuthorizer(aclDecision);
        return new ForumAuthorizer(accessResolver, policyEngine, aclAuthorizer);
    }

    private Forum forum() {
        return new Forum(
            1L,
            ForumSlug.of("general"),
            "General",
            "",
            ForumType.COMMON,
            Map.of(),
            1L,
            "admin",
            java.time.OffsetDateTime.now(),
            1L,
            "admin",
            java.time.OffsetDateTime.now(),
            0L
        );
    }

    private static class StubPolicyEngine extends ForumPolicyEngine {
        private final PolicyDecision decision;

        private StubPolicyEngine(PolicyDecision decision) {
            super(new ForumPolicyRegistry(java.util.List.of(
                new CommonBoardTypePolicy(),
                new NoticeBoardTypePolicy(),
                new SecretBoardTypePolicy(false),
                new AdminOnlyBoardTypePolicy()
            )));
            this.decision = decision;
        }

        @Override
        public PolicyDecision decide(studio.one.application.forums.domain.type.ForumType forumType,
                                     PermissionAction action, boolean isMember, boolean isAdmin, boolean isOwner,
                                     boolean isLocked) {
            return decision;
        }
    }

    private static class StubAclAuthorizer extends ForumAclAuthorizer {
        private final PolicyDecision decision;

        private StubAclAuthorizer(PolicyDecision decision) {
            super(null);
            this.decision = decision;
        }

        @Override
        public PolicyDecision decide(Long forumId, Long categoryId, Set<String> roles, Long ownerId,
                                     Long userId, String username, PermissionAction action) {
            return decision;
        }

        @Override
        public java.util.Map<Long, PolicyDecision> decideForumsBulk(java.util.Collection<Long> forumIds,
                                                                    PermissionAction action, Set<String> roles,
                                                                    Long userId, String username) {
            return java.util.Map.of();
        }
    }

    private static class EmptyForumMemberRepo implements ForumMemberRepository {
        @Override
        public Optional<ForumMemberRole> findRole(long forumId, long userId) {
            return Optional.empty();
        }

        @Override
        public Set<ForumMemberRole> findRoles(long forumId, long userId) {
            return Set.of();
        }

        @Override
        public Map<Long, Set<ForumMemberRole>> findRolesByUserId(long userId) {
            return Map.of();
        }

        @Override
        public List<studio.one.application.forums.domain.model.ForumMember> listMembers(long forumId, int page, int size) {
            return List.of();
        }

        @Override
        public void upsertMemberRole(long forumId, long userId, ForumMemberRole role, Long actorId) {
            throw new UnsupportedOperationException("Not needed for test");
        }

        @Override
        public void removeMember(long forumId, long userId) {
            throw new UnsupportedOperationException("Not needed for test");
        }
    }
}
