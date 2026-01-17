package studio.one.application.forums.service.authz.policy;

import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.service.authz.PolicyDecision;

public class AdminOnlyBoardTypePolicy implements BoardTypePolicy {
    @Override
    public PolicyDecision decide(PermissionAction action, boolean isMember, boolean isAdmin, boolean isOwner, boolean isLocked) {
        return isAdmin ? PolicyDecision.ALLOW : PolicyDecision.DENY;
    }
}
