package studio.one.application.forums.service.authz;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import studio.one.application.forums.domain.acl.Effect;
import studio.one.application.forums.domain.acl.ForumAclRule;
import studio.one.application.forums.domain.acl.Ownership;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.acl.SubjectType;
import studio.one.application.forums.domain.repository.ForumAclRuleRepository;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.domain.repository.TopicRepository;

@Service
public class ForumAuthorizationService {
    private final ForumAclRuleRepository forumAclRuleRepository;
    private final TopicRepository topicRepository;
    private final PostRepository postRepository;

    public ForumAuthorizationService(ForumAclRuleRepository forumAclRuleRepository,
                                     TopicRepository topicRepository,
                                     PostRepository postRepository) {
        this.forumAclRuleRepository = forumAclRuleRepository;
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
    }

    public boolean canForum(long forumId, String role, PermissionAction action) {
        return canForum(forumId, Set.of(role), action);
    }

    public boolean canForum(long forumId, Collection<String> roles, PermissionAction action) {
        return decideForum(forumId, roles, action) == PolicyDecision.ALLOW;
    }

    public boolean canCategory(long forumId, Long categoryId, String role, PermissionAction action) {
        return canCategory(forumId, categoryId, Set.of(role), action);
    }

    public boolean canCategory(long forumId, Long categoryId, Collection<String> roles, PermissionAction action) {
        return decideCategory(forumId, categoryId, roles, action) == PolicyDecision.ALLOW;
    }

    public boolean canTopic(long topicId, Collection<String> roles, PermissionAction action) {
        return canTopic(topicId, roles, action, null, null, null);
    }

    public boolean canTopic(long topicId, Collection<String> roles, PermissionAction action,
                            Set<Long> roleIds, Long userId, String username) {
        Set<String> roleSet = toRoleSet(roles);
        return topicRepository.findById(topicId)
            .map(topic -> decide(topic.forumId(), topic.categoryId(), roleSet, roleIds, userId, username,
                action, null, null) == PolicyDecision.ALLOW)
            .orElse(false);
    }

    public boolean canPost(long postId, String role, PermissionAction action, long currentUserId) {
        return canPost(postId, Set.of(role), action, currentUserId);
    }

    public boolean canPost(long postId, Collection<String> roles, PermissionAction action, Long currentUserId) {
        return canPost(postId, roles, action, currentUserId, null, null);
    }

    public boolean canPost(long postId, Collection<String> roles, PermissionAction action,
                           Long currentUserId, Set<Long> roleIds, String username) {
        Set<String> roleSet = toRoleSet(roles);
        return postRepository.findById(postId)
            .flatMap(post -> topicRepository.findById(post.topicId())
                .map(topic -> decide(topic.forumId(), topic.categoryId(), roleSet, roleIds, currentUserId, username,
                    action, post.createdById(), currentUserId) == PolicyDecision.ALLOW))
            .orElse(false);
    }

    public PolicyDecision decideForum(long forumId, Collection<String> roles, PermissionAction action) {
        return decide(forumId, null, toRoleSet(roles), Set.of(), null, null, action, null, null);
    }

    public PolicyDecision decideCategory(long forumId, Long categoryId, Collection<String> roles, PermissionAction action) {
        return decide(forumId, categoryId, toRoleSet(roles), Set.of(), null, null, action, null, null);
    }

    public PolicyDecision decideWithOwnership(long forumId, Long categoryId, Collection<String> roles,
                                              PermissionAction action, Long ownerId, Long currentUserId) {
        return decideWithOwnership(forumId, categoryId, roles, Set.of(), action, ownerId, currentUserId, null);
    }

    public PolicyDecision decideWithOwnership(long forumId, Long categoryId, Collection<String> roles,
                                              Set<Long> roleIds, PermissionAction action, Long ownerId,
                                              Long currentUserId, String username) {
        return decide(forumId, categoryId, toRoleSet(roles), roleIds, currentUserId, username, action, ownerId, currentUserId);
    }

    public java.util.Map<Long, PolicyDecision> decideForumsBulk(Collection<Long> forumIds, Collection<String> roles,
                                                                PermissionAction action, Long userId, String username) {
        Set<Long> forumIdSet = forumIds == null ? Set.of() : new java.util.HashSet<>(forumIds);
        if (forumIdSet.isEmpty()) {
            return java.util.Map.of();
        }
        Set<String> roleNames = toRoleSet(roles);
        List<ForumAclRule> rules = forumAclRuleRepository.findRulesBulk(
            forumIdSet, null, action, roleNames, Set.of(), userId, username);
        java.util.Map<Long, List<ForumAclRule>> grouped = new java.util.HashMap<>();
        for (ForumAclRule rule : rules) {
            grouped.computeIfAbsent(rule.forumId(), ignored -> new java.util.ArrayList<>()).add(rule);
        }
        java.util.Map<Long, PolicyDecision> decisions = new java.util.HashMap<>();
        for (Long forumId : forumIdSet) {
            List<ForumAclRule> forumRules = grouped.getOrDefault(forumId, List.of());
            decisions.put(forumId, evaluateRules(forumRules, null, userId));
        }
        return java.util.Collections.unmodifiableMap(decisions);
    }

    private PolicyDecision decide(long forumId, Long categoryId, Set<String> roleNames, Set<Long> roleIds,
                                  Long userId, String username, PermissionAction action,
                                  Long ownerId, Long currentUserId) {
        boolean hasSubjects = (roleNames != null && !roleNames.isEmpty())
            || (roleIds != null && !roleIds.isEmpty())
            || userId != null
            || (username != null && !username.isBlank());
        if (!hasSubjects) {
            return PolicyDecision.ABSTAIN;
        }
        List<ForumAclRule> rules = forumAclRuleRepository.findRules(forumId, categoryId, action, roleNames, roleIds, userId, username);
        if (rules.isEmpty()) {
            return PolicyDecision.ABSTAIN;
        }
        return evaluateRules(rules, ownerId, currentUserId);
    }

    private PolicyDecision evaluateRules(List<ForumAclRule> rules, Long ownerId, Long currentUserId) {
        if (rules == null || rules.isEmpty()) {
            return PolicyDecision.ABSTAIN;
        }
        rules.sort(ForumAuthorizationService::compareRules);
        boolean allowed = false;
        for (ForumAclRule rule : rules) {
            if (!ownershipMatches(rule.ownership(), ownerId, currentUserId)) {
                continue;
            }
            if (rule.effect() == Effect.DENY) {
                return PolicyDecision.DENY;
            }
            if (rule.effect() == Effect.ALLOW) {
                allowed = true;
            }
        }
        return allowed ? PolicyDecision.ALLOW : PolicyDecision.ABSTAIN;
    }

    private static int compareRules(ForumAclRule left, ForumAclRule right) {
        int categoryCompare = Boolean.compare(left.categoryId() != null, right.categoryId() != null);
        if (categoryCompare != 0) {
            return -categoryCompare;
        }
        int priorityCompare = Integer.compare(left.priority(), right.priority());
        if (priorityCompare != 0) {
            return -priorityCompare;
        }
        int subjectCompare = Integer.compare(subjectSpecificity(left), subjectSpecificity(right));
        return -subjectCompare;
    }

    private static int subjectSpecificity(ForumAclRule rule) {
        if (rule.subjectType() == null) {
            return 0;
        }
        return rule.subjectType() == SubjectType.USER ? 1 : 0;
    }

    private boolean ownershipMatches(Ownership ownership, Long ownerId, Long currentUserId) {
        if (ownerId == null || currentUserId == null) {
            return ownership == Ownership.ANY;
        }
        return switch (ownership) {
            case ANY -> true;
            case OWNER_ONLY -> ownerId.equals(currentUserId);
            case NON_OWNER_ONLY -> !ownerId.equals(currentUserId);
        };
    }

    private Set<String> toRoleSet(Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
            .filter(role -> role != null && !role.isBlank())
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public boolean canBoard(long boardId, String role, PermissionAction action) {
        return canForum(boardId, role, action);
    }

    public boolean canBoard(long boardId, Collection<String> roles, PermissionAction action) {
        return canForum(boardId, roles, action);
    }

    public PolicyDecision decideBoard(long boardId, Collection<String> roles, PermissionAction action) {
        return decideForum(boardId, roles, action);
    }
}
