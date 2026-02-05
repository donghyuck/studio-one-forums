package studio.one.application.forums.service.authz.policy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.service.authz.PolicyDecision;

@Component
public class SecretBoardTypePolicy implements ForumTypePolicy {
    private final boolean listVisibleToMembers;

    public SecretBoardTypePolicy(@Value("${studio.features.forums.authz.secret-list-visible:false}") boolean listVisibleToMembers) {
        this.listVisibleToMembers = listVisibleToMembers;
    }

    @Override
    public ForumType type() {
        return ForumType.SECRET;
    }

    @Override
    public PolicyDecision decide(PermissionAction action, boolean isMember, boolean isAdmin, boolean isOwner, boolean isLocked) {
        if (action == PermissionAction.READ_TOPIC_CONTENT || action == PermissionAction.READ_ATTACHMENT) {
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
            || action == PermissionAction.EDIT_POST || action == PermissionAction.DELETE_POST
            || action == PermissionAction.UPLOAD_ATTACHMENT) {
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
