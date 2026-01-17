package studio.one.application.forums.domain.repository;

import java.util.List;
import java.util.Set;
import studio.one.application.forums.domain.acl.ForumAclRule;
import studio.one.application.forums.domain.acl.PermissionAction;

public interface ForumAclRuleRepository {
    List<ForumAclRule> findRules(long boardId, Long categoryId, PermissionAction action,
                                 Set<String> roleNames, Set<Long> roleIds, Long userId, String username);
}
