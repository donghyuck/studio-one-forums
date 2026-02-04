package studio.one.application.forums.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import studio.one.application.forums.domain.acl.ForumAclRule;
import studio.one.application.forums.domain.acl.PermissionAction;

public interface ForumAclRuleRepository {
    List<ForumAclRule> findRules(long forumId, Long categoryId, PermissionAction action,
                                 Set<String> roleNames, Set<Long> roleIds, Long userId, String username);

    List<ForumAclRule> findRulesBulk(Set<Long> forumIds, Long categoryId, PermissionAction action,
                                     Set<String> roleNames, Set<Long> roleIds, Long userId, String username);

    List<ForumAclRule> findByForumId(long forumId);

    Optional<ForumAclRule> findById(long ruleId);

    ForumAclRule save(ForumAclRule rule);

    void delete(ForumAclRule rule);
}
