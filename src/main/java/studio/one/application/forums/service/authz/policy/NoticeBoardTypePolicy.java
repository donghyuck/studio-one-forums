package studio.one.application.forums.service.authz.policy;

import org.springframework.stereotype.Component;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.service.authz.PolicyDecision;

@Component
public class NoticeBoardTypePolicy implements ForumTypePolicy {
    @Override
    public ForumType type() {
        return ForumType.NOTICE;
    }

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
