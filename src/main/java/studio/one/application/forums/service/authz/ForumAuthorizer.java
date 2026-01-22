package studio.one.application.forums.service.authz;

import java.util.Set;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.type.ForumType;

@Component
public class ForumAuthorizer {
    private final ForumAccessResolver accessResolver;
    private final ForumPolicyEngine policyEngine;
    private final ForumAclAuthorizer aclAuthorizer;
    private final ObjectProvider<ForumAuthzRequestCache> cacheProvider;

    public ForumAuthorizer(ForumAccessResolver accessResolver,
                           ForumPolicyEngine policyEngine,
                           ForumAclAuthorizer aclAuthorizer,
                           ObjectProvider<ForumAuthzRequestCache> cacheProvider) {
        this.accessResolver = accessResolver;
        this.policyEngine = policyEngine;
        this.aclAuthorizer = aclAuthorizer;
        this.cacheProvider = cacheProvider;
    }

    public ForumAuthorizer(ForumAccessResolver accessResolver,
                           ForumPolicyEngine policyEngine,
                           ForumAclAuthorizer aclAuthorizer) {
        this(accessResolver, policyEngine, aclAuthorizer, emptyProvider());
    }

    private static ObjectProvider<ForumAuthzRequestCache> emptyProvider() {
        return new ObjectProvider<>() {
            @Override
            public ForumAuthzRequestCache getObject() {
                return null;
            }

            @Override
            public ForumAuthzRequestCache getObject(Object... args) {
                return null;
            }

            @Override
            public ForumAuthzRequestCache getIfAvailable() {
                return null;
            }

            @Override
            public ForumAuthzRequestCache getIfUnique() {
                return null;
            }

            @Override
            public java.util.stream.Stream<ForumAuthzRequestCache> stream() {
                return java.util.stream.Stream.empty();
            }

            @Override
            public java.util.stream.Stream<ForumAuthzRequestCache> orderedStream() {
                return java.util.stream.Stream.empty();
            }
        };
    }

    public AuthorizationDecision authorize(Forum forum, Long categoryId, PermissionAction action, Long ownerId,
                                           boolean locked, ForumAccessContext context, Set<String> effectiveRoles,
                                           boolean allowDbRules) {
        if (forum == null) {
            return AuthorizationDecision.deny(PolicyDecision.DENY, PolicyDecision.ABSTAIN, DenyReason.POLICY_DENY);
        }
        boolean isAdmin = accessResolver.isAdmin(effectiveRoles);
        boolean isMember = context != null && context.isMember();
        boolean isOwner = ownerId != null && context != null && context.getUserId() != null && ownerId.equals(context.getUserId());
        PolicyDecision policyDecision = policyEngine.decide(forum.type(), action, isMember, isAdmin, isOwner, locked);
        if (policyDecision == PolicyDecision.DENY) {
            return AuthorizationDecision.deny(policyDecision, PolicyDecision.ABSTAIN, DenyReason.POLICY_DENY);
        }
        PolicyDecision aclDecision = allowDbRules
            ? aclAuthorizer.decide(forum.id(), categoryId, effectiveRoles, ownerId,
                context != null ? context.getUserId() : null, context != null ? context.getUsername() : null, action)
            : PolicyDecision.ABSTAIN;
        if (aclDecision == PolicyDecision.DENY) {
            return AuthorizationDecision.deny(policyDecision, aclDecision, DenyReason.ACL_DENY);
        }
        if (aclDecision == PolicyDecision.ALLOW) {
            return AuthorizationDecision.allow(policyDecision, aclDecision);
        }
        if (policyDecision == PolicyDecision.ALLOW) {
            return AuthorizationDecision.allow(policyDecision, aclDecision);
        }
        return AuthorizationDecision.deny(policyDecision, aclDecision, DenyReason.POLICY_ABSTAIN);
    }

    public AuthorizationDecision authorizeWithAclDecision(Forum forum, Long categoryId, PermissionAction action, Long ownerId,
                                                          boolean locked, ForumAccessContext context, Set<String> effectiveRoles,
                                                          PolicyDecision aclDecision) {
        if (forum == null) {
            return AuthorizationDecision.deny(PolicyDecision.DENY, PolicyDecision.ABSTAIN, DenyReason.POLICY_DENY);
        }
        boolean isAdmin = accessResolver.isAdmin(effectiveRoles);
        boolean isMember = context != null && context.isMember();
        boolean isOwner = ownerId != null && context != null && context.getUserId() != null && ownerId.equals(context.getUserId());
        PolicyDecision policyDecision = policyEngine.decide(forum.type(), action, isMember, isAdmin, isOwner, locked);
        if (policyDecision == PolicyDecision.DENY) {
            return AuthorizationDecision.deny(policyDecision, PolicyDecision.ABSTAIN, DenyReason.POLICY_DENY);
        }
        PolicyDecision resolvedAcl = aclDecision != null ? aclDecision : PolicyDecision.ABSTAIN;
        if (resolvedAcl == PolicyDecision.DENY) {
            return AuthorizationDecision.deny(policyDecision, resolvedAcl, DenyReason.ACL_DENY);
        }
        if (resolvedAcl == PolicyDecision.ALLOW) {
            return AuthorizationDecision.allow(policyDecision, resolvedAcl);
        }
        if (policyDecision == PolicyDecision.ALLOW) {
            return AuthorizationDecision.allow(policyDecision, resolvedAcl);
        }
        return AuthorizationDecision.deny(policyDecision, resolvedAcl, DenyReason.POLICY_ABSTAIN);
    }

    public java.util.Map<Long, PolicyDecision> aclDecisionsForForums(java.util.Collection<Long> forumIds,
                                                                     PermissionAction action, ForumAccessContext context) {
        if (context == null) {
            return java.util.Map.of();
        }
        ForumAuthzRequestCache cache = cacheProvider.getIfAvailable();
        String key = action.name() + "|" + context.getUserId() + "|" + context.getUsername() + "|" + context.getRoles().hashCode();
        if (cache == null) {
            return aclAuthorizer.decideForumsBulk(forumIds, action, context.getRoles(), context.getUserId(), context.getUsername());
        }
        return cache.getAclDecisions(key, forumIds,
            ids -> aclAuthorizer.decideForumsBulk(ids, action, context.getRoles(), context.getUserId(), context.getUsername()));
    }
}
