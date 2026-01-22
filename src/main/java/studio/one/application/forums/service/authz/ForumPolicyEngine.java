package studio.one.application.forums.service.authz;

import org.springframework.stereotype.Component;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.type.ForumType;

@Component
public class ForumPolicyEngine {
    private final ForumPolicyRegistry registry;

    public ForumPolicyEngine(ForumPolicyRegistry registry) {
        this.registry = registry;
    }

    public PolicyDecision decide(ForumType forumType, PermissionAction action,
                                 boolean isMember, boolean isAdmin, boolean isOwner, boolean isLocked) {
        return registry.get(forumType).decide(action, isMember, isAdmin, isOwner, isLocked);
    }
}
