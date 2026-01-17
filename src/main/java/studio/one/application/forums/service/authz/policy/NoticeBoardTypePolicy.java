package studio.one.application.forums.service.authz.policy;

import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.service.authz.PolicyDecision;

public class NoticeBoardTypePolicy implements BoardTypePolicy {
    @Override
    public PolicyDecision decide(PermissionAction action, boolean isMember, boolean isAdmin, boolean isOwner, boolean isLocked) {
        if (action == PermissionAction.READ_BOARD
            || action == PermissionAction.READ_TOPIC_LIST
            || action == PermissionAction.READ_TOPIC_CONTENT) {
            return PolicyDecision.ALLOW;
        }
        return isAdmin ? PolicyDecision.ALLOW : PolicyDecision.DENY;
    }
}
