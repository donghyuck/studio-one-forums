package studio.one.application.forums.service.authz.policy;

import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.service.authz.PolicyDecision;

public interface ForumTypePolicy {
    ForumType type();

    PolicyDecision decide(PermissionAction action, boolean isMember, boolean isAdmin, boolean isOwner, boolean isLocked);
}
