package studio.one.application.forums.service.authz;

import java.util.Set;
import org.springframework.stereotype.Component;
import studio.one.application.forums.domain.acl.PermissionAction;

@Component
public class ForumAclAuthorizer {
    private final ForumAuthorizationService forumAuthorizationService;

    public ForumAclAuthorizer(ForumAuthorizationService forumAuthorizationService) {
        this.forumAuthorizationService = forumAuthorizationService;
    }

    public PolicyDecision decide(Long forumId, Long categoryId, Set<String> roles, Long ownerId,
                                 Long userId, String username, PermissionAction action) {
        if (forumId == null) {
            return PolicyDecision.ABSTAIN;
        }
        return forumAuthorizationService.decideWithOwnership(
            forumId,
            categoryId,
            roles,
            Set.of(),
            action,
            ownerId,
            userId,
            username
        );
    }

    public java.util.Map<Long, PolicyDecision> decideForumsBulk(java.util.Collection<Long> forumIds,
                                                                 PermissionAction action, Set<String> roles,
                                                                 Long userId, String username) {
        return forumAuthorizationService.decideForumsBulk(forumIds, roles, action, userId, username);
    }
}
