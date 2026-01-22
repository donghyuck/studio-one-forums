package studio.one.application.forums.service.authz;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import studio.one.application.forums.domain.type.ForumMemberRole;

@Component
@RequestScope
public class ForumAuthzRequestCache {
    private Long memberRolesUserId;
    private Map<Long, Set<ForumMemberRole>> memberRolesByForumId;
    private final Map<String, Map<Long, PolicyDecision>> aclDecisions = new HashMap<>();

    public Map<Long, Set<ForumMemberRole>> getMemberRoles(Long userId,
                                                          Supplier<Map<Long, Set<ForumMemberRole>>> loader) {
        if (userId == null) {
            return Map.of();
        }
        if (memberRolesByForumId == null || !userId.equals(memberRolesUserId)) {
            memberRolesByForumId = new HashMap<>(loader.get());
            memberRolesUserId = userId;
        }
        return memberRolesByForumId;
    }

    public Map<Long, PolicyDecision> getAclDecisions(String key, Collection<Long> forumIds,
                                                     Function<Collection<Long>, Map<Long, PolicyDecision>> loader) {
        if (forumIds == null || forumIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, PolicyDecision> existing = aclDecisions.get(key);
        if (existing == null) {
            Map<Long, PolicyDecision> loaded = loader.apply(forumIds);
            existing = new HashMap<>(loaded);
            aclDecisions.put(key, existing);
            return existing;
        }
        java.util.Set<Long> missing = new java.util.HashSet<>();
        for (Long forumId : forumIds) {
            if (!existing.containsKey(forumId)) {
                missing.add(forumId);
            }
        }
        if (!missing.isEmpty()) {
            Map<Long, PolicyDecision> loaded = loader.apply(missing);
            existing.putAll(loaded);
        }
        return existing;
    }
}
