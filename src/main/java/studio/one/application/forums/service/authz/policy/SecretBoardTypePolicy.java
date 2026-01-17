package studio.one.application.forums.service.authz.policy;

import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.service.authz.PolicyDecision;

public class SecretBoardTypePolicy implements BoardTypePolicy {
    private final boolean listVisibleToMembers;

    public SecretBoardTypePolicy(boolean listVisibleToMembers) {
        this.listVisibleToMembers = listVisibleToMembers;
    }

    @Override
    public PolicyDecision decide(PermissionAction action, boolean isMember, boolean isAdmin, boolean isOwner, boolean isLocked) {
        if (action == PermissionAction.READ_TOPIC_CONTENT) {
            return (isOwner || isAdmin) ? PolicyDecision.ALLOW : PolicyDecision.DENY;
        }
        if (action == PermissionAction.READ_TOPIC_LIST || action == PermissionAction.READ_BOARD) {
            if (listVisibleToMembers && isMember) {
                return PolicyDecision.ALLOW;
            }
            return PolicyDecision.ABSTAIN;
        }
        if (action == PermissionAction.CREATE_TOPIC || action == PermissionAction.REPLY_POST) {
            if (action == PermissionAction.REPLY_POST && isLocked && !isAdmin) {
                return PolicyDecision.DENY;
            }
            return isMember ? PolicyDecision.ALLOW : PolicyDecision.DENY;
        }
        if (action == PermissionAction.EDIT_TOPIC || action == PermissionAction.DELETE_TOPIC
            || action == PermissionAction.EDIT_POST || action == PermissionAction.DELETE_POST) {
            return (isOwner || isAdmin) ? PolicyDecision.ALLOW : PolicyDecision.DENY;
        }
        if (action == PermissionAction.PIN_TOPIC || action == PermissionAction.LOCK_TOPIC
            || action == PermissionAction.HIDE_POST || action == PermissionAction.MODERATE
            || action == PermissionAction.MANAGE_BOARD) {
            return isAdmin ? PolicyDecision.ALLOW : PolicyDecision.DENY;
        }
        return PolicyDecision.ABSTAIN;
    }
}
