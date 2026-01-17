package studio.one.application.forums.persistence.jpa;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import studio.one.application.forums.domain.acl.IdentifierType;
import studio.one.application.forums.domain.acl.ForumAclRule;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.acl.SubjectType;
import studio.one.application.forums.domain.repository.ForumAclRuleRepository;
import studio.one.application.forums.persistence.jpa.entity.ForumAclRuleEntity;
import studio.one.application.forums.persistence.jpa.repo.ForumAclRuleJpaRepository;

@Repository
public class ForumAclRuleRepositoryAdapter implements ForumAclRuleRepository {
    private final ForumAclRuleJpaRepository forumAclRuleJpaRepository;

    public ForumAclRuleRepositoryAdapter(ForumAclRuleJpaRepository forumAclRuleJpaRepository) {
        this.forumAclRuleJpaRepository = forumAclRuleJpaRepository;
    }

    @Override
    public List<ForumAclRule> findRules(long boardId, Long categoryId, PermissionAction action,
                                        Set<String> roleNames, Set<Long> roleIds, Long userId, String username) {
        boolean hasRoleNames = roleNames != null && !roleNames.isEmpty();
        boolean hasRoleIds = roleIds != null && !roleIds.isEmpty();
        boolean hasUserId = userId != null;
        boolean hasUsername = username != null && !username.isBlank();
        if (!hasRoleNames && !hasRoleIds && !hasUserId && !hasUsername) {
            return List.of();
        }
        Set<String> roleNamesSafe = hasRoleNames ? roleNames : Set.of("__none__");
        Set<Long> roleIdsSafe = hasRoleIds ? roleIds : Set.of(-1L);
        return forumAclRuleJpaRepository.findRules(boardId, categoryId, action, roleNamesSafe, roleIdsSafe, userId,
                username, hasRoleNames, hasRoleIds, hasUserId, hasUsername)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    private ForumAclRule toDomain(ForumAclRuleEntity entity) {
        SubjectType subjectType = entity.getSubjectType() != null ? entity.getSubjectType() : SubjectType.ROLE;
        IdentifierType identifierType = entity.getIdentifierType() != null ? entity.getIdentifierType() : IdentifierType.NAME;
        String subjectName = entity.getSubjectName();
        if (subjectName == null && identifierType == IdentifierType.NAME) {
            subjectName = entity.getRole();
        }
        return new ForumAclRule(
            entity.getRuleId(),
            entity.getBoardId(),
            entity.getCategoryId(),
            subjectType,
            identifierType,
            entity.getSubjectId(),
            subjectName,
            entity.getRole(),
            entity.getAction(),
            entity.getEffect(),
            entity.getOwnership(),
            entity.getPriority(),
            entity.isEnabled(),
            entity.getCreatedById(),
            entity.getCreatedAt(),
            entity.getUpdatedById(),
            entity.getUpdatedAt()
        );
    }
}
