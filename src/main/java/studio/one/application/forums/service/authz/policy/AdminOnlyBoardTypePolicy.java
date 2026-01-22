package studio.one.application.forums.service.authz.policy;

import org.springframework.stereotype.Component;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.service.authz.PolicyDecision;

@Component
public class AdminOnlyBoardTypePolicy implements ForumTypePolicy {
    @Override
    public ForumType type() {
        return ForumType.ADMIN_ONLY;
    }

    @Override
    public PolicyDecision decide(PermissionAction action, boolean isMember, boolean isAdmin, boolean isOwner, boolean isLocked) {
        return isAdmin ? PolicyDecision.ALLOW : PolicyDecision.DENY;
    }
}
