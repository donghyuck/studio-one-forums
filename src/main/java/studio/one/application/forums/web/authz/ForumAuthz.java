package studio.one.application.forums.web.authz;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.exception.PostNotFoundException;
import studio.one.application.forums.domain.exception.TopicNotFoundException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.CategoryRepository;
import studio.one.application.forums.domain.repository.ForumMemberRepository;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.domain.type.ForumMemberRole;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.service.authz.ForumAuthorizationService;
import studio.one.application.forums.service.authz.PolicyDecision;
import studio.one.application.forums.service.authz.policy.AdminOnlyBoardTypePolicy;
import studio.one.application.forums.service.authz.policy.BoardTypePolicy;
import studio.one.application.forums.service.authz.policy.CommonBoardTypePolicy;
import studio.one.application.forums.service.authz.policy.NoticeBoardTypePolicy;
import studio.one.application.forums.service.authz.policy.SecretBoardTypePolicy;

@Component("forumAuthz")
public class ForumAuthz {
    private final ForumAuthorizationService forumAuthorizationService;
    private final ForumRepository forumRepository;
    private final CategoryRepository categoryRepository;
    private final TopicRepository topicRepository;
    private final PostRepository postRepository;
    private final ForumMemberRepository forumMemberRepository;
    private final Set<String> adminRoles;
    private final Map<ForumType, BoardTypePolicy> policies;
    private final boolean secretListVisible;

    public ForumAuthz(ForumAuthorizationService forumAuthorizationService,
                      ForumRepository forumRepository,
                      CategoryRepository categoryRepository,
                      TopicRepository topicRepository,
                      PostRepository postRepository,
                      ForumMemberRepository forumMemberRepository,
                      @Value("${studio.features.forums.authz.admin-roles:ROLE_ADMIN,ADMIN}") Collection<String> adminRoles,
                      @Value("${studio.features.forums.authz.secret-list-visible:false}") boolean secretListVisible) {
        this.forumAuthorizationService = forumAuthorizationService;
        this.forumRepository = forumRepository;
        this.categoryRepository = categoryRepository;
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
        this.forumMemberRepository = forumMemberRepository;
        this.adminRoles = normalizeRoles(adminRoles);
        this.secretListVisible = secretListVisible;
        this.policies = new EnumMap<>(ForumType.class);
        this.policies.put(ForumType.COMMON, new CommonBoardTypePolicy());
        this.policies.put(ForumType.NOTICE, new NoticeBoardTypePolicy());
        this.policies.put(ForumType.SECRET, new SecretBoardTypePolicy(secretListVisible));
        this.policies.put(ForumType.ADMIN_ONLY, new AdminOnlyBoardTypePolicy());
    }

    public boolean canListForums(String action) {
        PermissionAction permission = PermissionAction.from(action);
        return canListForums(resolveRoles(), permission);
    }

    public boolean canListForums(Collection<String> roles, String action) {
        return canListForums(roles, PermissionAction.from(action));
    }

    private boolean canListForums(Collection<String> roles, PermissionAction permission) {
        List<Forum> forums = forumRepository.findAll();
        return !filterForumsByAccess(forums, permission).isEmpty();
    }

    public ForumListVisibility listVisibility() {
        Set<String> roleSet = resolveRoles();
        Long userId = resolveUserId();
        return new ForumListVisibility(hasAdminRole(roleSet), userId != null, secretListVisible, userId);
    }

    public List<Forum> filterForumsByAccess(List<Forum> forums, PermissionAction action) {
        if (forums == null || forums.isEmpty()) {
            return List.of();
        }
        Set<String> roleSet = resolveRoles();
        Long userId = resolveUserId();
        String username = resolveUsername();
        Map<Long, Set<ForumMemberRole>> memberRoles = userId != null
            ? forumMemberRepository.findRolesByUserId(userId)
            : Map.of();
        List<Forum> allowed = new java.util.ArrayList<>();
        for (Forum forum : forums) {
            Set<String> effectiveRoles = effectiveRoles(roleSet, memberRoles.get(forum.id()));
            boolean access = decideAccess(forum, null, action, null, userId, username,
                effectiveRoles, true, false);
            if (access) {
                allowed.add(forum);
            }
        }
        return allowed;
    }

    public boolean canBoard(Long boardId, String action) {
        if (boardId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        return forumRepository.findById(boardId)
            .map(forum -> decideAccess(forum, null, permission, null, resolveUserId(), resolveUsername(),
                effectiveRoles(forum.id(), resolveUserId(), resolveRoles()), true, false))
            .orElse(false);
    }

    public boolean canBoard(Long boardId, Collection<String> roles, String action) {
        if (boardId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        return forumRepository.findById(boardId)
            .map(forum -> decideAccess(forum, null, permission, null, resolveUserId(), resolveUsername(),
                effectiveRoles(forum.id(), resolveUserId(), normalizeRoles(roles)), true, false))
            .orElse(false);
    }

    public boolean canBoard(String forumSlug, String action) {
        if (forumSlug == null || forumSlug.isBlank()) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        return forumRepository.findBySlug(ForumSlug.of(forumSlug))
            .map(forum -> decideAccess(forum, null, permission, null, resolveUserId(), resolveUsername(),
                effectiveRoles(forum.id(), resolveUserId(), resolveRoles()), true, false))
            .orElseThrow(() -> ForumNotFoundException.bySlug(forumSlug));
    }

    public boolean canBoard(String forumSlug, Collection<String> roles, String action) {
        if (forumSlug == null || forumSlug.isBlank()) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        Set<String> roleSet = normalizeRoles(roles);
        return forumRepository.findBySlug(ForumSlug.of(forumSlug))
            .map(forum -> decideAccess(forum, null, permission, null, resolveUserId(), resolveUsername(),
                effectiveRoles(forum.id(), resolveUserId(), roleSet), true, false))
            .orElseThrow(() -> ForumNotFoundException.bySlug(forumSlug));
    }

    public boolean canCategory(Long categoryId, String action) {
        if (categoryId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        return categoryRepository.findById(categoryId)
            .flatMap(category -> forumRepository.findById(category.forumId())
                .map(forum -> decideAccess(forum, category.id(), permission, null, resolveUserId(), resolveUsername(),
                    effectiveRoles(forum.id(), resolveUserId(), resolveRoles()), true, false)))
            .orElse(false);
    }

    public boolean canCategory(Long categoryId, Collection<String> roles, String action) {
        if (categoryId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        Set<String> roleSet = normalizeRoles(roles);
        return categoryRepository.findById(categoryId)
            .flatMap(category -> forumRepository.findById(category.forumId())
                .map(forum -> decideAccess(forum, category.id(), permission, null, resolveUserId(), resolveUsername(),
                    effectiveRoles(forum.id(), resolveUserId(), roleSet), true, false)))
            .orElse(false);
    }

    public boolean canTopic(Long topicId, String action) {
        if (topicId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        Set<String> roles = resolveRoles();
        Long userId = resolveUserId();
        return topicRepository.findById(topicId)
            .flatMap(topic -> forumRepository.findById(topic.forumId())
                .map(forum -> applyTopicAccess(forum, topicId, permission,
                    decideAccess(forum, topic.categoryId(), permission, topic.createdById(), userId, resolveUsername(),
                        effectiveRoles(forum.id(), userId, roles), true, topic.locked()))))
            .orElseThrow(() -> TopicNotFoundException.byId(topicId));
    }

    public boolean canTopic(Long topicId, Collection<String> roles, String action) {
        if (topicId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        Set<String> roleSet = normalizeRoles(roles);
        Long userId = resolveUserId();
        return topicRepository.findById(topicId)
            .flatMap(topic -> forumRepository.findById(topic.forumId())
                .map(forum -> applyTopicAccess(forum, topicId, permission,
                    decideAccess(forum, topic.categoryId(), permission, topic.createdById(), userId, resolveUsername(),
                        effectiveRoles(forum.id(), userId, roleSet), true, topic.locked()))))
            .orElseThrow(() -> TopicNotFoundException.byId(topicId));
    }

    public boolean canPost(Long postId, String action) {
        if (postId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        return canPost(postId, resolveRoles(), permission.name(), resolveUserId());
    }

    public boolean canPost(Long postId, Collection<String> roles, String action, Long userId) {
        if (postId == null) {
            return false;
        }
        if (userId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        Set<String> roleSet = normalizeRoles(roles);
        return postRepository.findById(postId)
            .flatMap(post -> topicRepository.findById(post.topicId())
                .flatMap(topic -> forumRepository.findById(topic.forumId())
                    .map(forum -> applyPostAccess(forum, postId, permission,
                        decideAccess(forum, topic.categoryId(), permission, post.createdById(), userId, resolveUsername(),
                            effectiveRoles(forum.id(), userId, roleSet), true, topic.locked())))))
            .orElse(false);
    }

    private boolean decideAccess(Forum forum, Long categoryId, PermissionAction action, Long ownerId, Long userId,
                                 String username, Set<String> roles, boolean allowDbRules, boolean locked) {
        if (forum == null) {
            return false;
        }
        ForumType type = forum.type() != null ? forum.type() : ForumType.COMMON;
        BoardTypePolicy policy = policies.getOrDefault(type, new CommonBoardTypePolicy());
        boolean isAdmin = hasAdminRole(roles);
        boolean isMember = userId != null;
        boolean isOwner = ownerId != null && userId != null && ownerId.equals(userId);
        PolicyDecision typeDecision = policy.decide(action, isMember, isAdmin, isOwner, locked);
        if (typeDecision == PolicyDecision.DENY) {
            return false;
        }
        PolicyDecision ruleDecision = allowDbRules
            ? forumAuthorizationService.decideWithOwnership(forum.id(), categoryId, roles, Set.of(),
                action, ownerId, userId, username)
            : PolicyDecision.ABSTAIN;
        if (ruleDecision == PolicyDecision.DENY) {
            return false;
        }
        if (ruleDecision == PolicyDecision.ALLOW) {
            return true;
        }
        return typeDecision == PolicyDecision.ALLOW;
    }

    private boolean applyTopicAccess(Forum forum, Long topicId, PermissionAction action, boolean allowed) {
        if (allowed) {
            return true;
        }
        ForumType type = forum.type() != null ? forum.type() : ForumType.COMMON;
        if (type == ForumType.ADMIN_ONLY) {
            throw TopicNotFoundException.byId(topicId);
        }
        if (type == ForumType.SECRET && action == PermissionAction.READ_TOPIC_CONTENT) {
            throw TopicNotFoundException.byId(topicId);
        }
        return false;
    }

    private boolean applyPostAccess(Forum forum, Long postId, PermissionAction action, boolean allowed) {
        if (allowed) {
            return true;
        }
        ForumType type = forum.type() != null ? forum.type() : ForumType.COMMON;
        if (type == ForumType.ADMIN_ONLY) {
            throw PostNotFoundException.byId(postId);
        }
        if (type == ForumType.SECRET && action == PermissionAction.READ_TOPIC_CONTENT) {
            throw PostNotFoundException.byId(postId);
        }
        return false;
    }

    private boolean hasAdminRole(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        if (adminRoles.isEmpty()) {
            return false;
        }
        return roles.stream().anyMatch(role -> adminRoles.contains(role) || "MODERATOR".equals(role));
    }

    private Set<String> effectiveRoles(Set<String> baseRoles, Set<ForumMemberRole> memberRoles) {
        Set<String> roles = baseRoles == null ? Set.of() : baseRoles;
        if (memberRoles == null || memberRoles.isEmpty()) {
            return roles;
        }
        Set<String> merged = new java.util.HashSet<>(roles);
        for (ForumMemberRole role : memberRoles) {
            merged.add(mapMemberRole(role));
        }
        return java.util.Set.copyOf(merged);
    }

    private Set<String> effectiveRoles(Long forumId, Long userId, Set<String> baseRoles) {
        Set<String> roles = baseRoles == null ? Set.of() : baseRoles;
        if (forumId == null || userId == null) {
            return roles;
        }
        Set<ForumMemberRole> memberRoles = forumMemberRepository.findRoles(forumId, userId);
        if (memberRoles.isEmpty()) {
            return roles;
        }
        Set<String> merged = new java.util.HashSet<>(roles);
        for (ForumMemberRole role : memberRoles) {
            merged.add(mapMemberRole(role));
        }
        return java.util.Set.copyOf(merged);
    }

    private String mapMemberRole(ForumMemberRole role) {
        return switch (role) {
            case OWNER, ADMIN -> "ADMIN";
            case MODERATOR -> "MODERATOR";
            case MEMBER -> "MEMBER";
        };
    }

    private Set<String> resolveRoles() {
        Authentication authentication = currentAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return Set.of();
        }
        return authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .filter(value -> value != null && !value.isBlank())
            .collect(Collectors.toUnmodifiableSet());
    }

    private Set<String> normalizeRoles(Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
            .filter(value -> value != null && !value.isBlank())
            .collect(Collectors.toUnmodifiableSet());
    }

    private Long resolveUserId() {
        Authentication authentication = currentAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        Long userId = resolveUserId(principal);
        if (userId != null) {
            return userId;
        }
        return resolveUserId(authentication.getDetails());
    }

    private Long resolveUserId(Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof Number) {
            return ((Number) source).longValue();
        }
        if (source instanceof Map<?, ?> map) {
            Object value = map.get("userId");
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
        }
        try {
            Method method = source.getClass().getMethod("getUserId");
            Object value = method.invoke(source);
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
        } catch (ReflectiveOperationException ex) {
            // ignore
        }
        try {
            Method method = source.getClass().getMethod("getId");
            Object value = method.invoke(source);
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
        } catch (ReflectiveOperationException ex) {
            // ignore
        }
        try {
            Field field = source.getClass().getDeclaredField("userId");
            field.setAccessible(true);
            Object value = field.get(source);
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
        } catch (ReflectiveOperationException ex) {
            // ignore
        }
        return null;
    }

    private String resolveUsername() {
        Authentication authentication = currentAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String name = authentication.getName();
        if (name != null && !name.isBlank() && !"anonymousUser".equals(name)) {
            return name;
        }
        Object principal = authentication.getPrincipal();
        String principalName = resolveUsername(principal);
        if (principalName != null && !principalName.isBlank()) {
            return principalName;
        }
        return resolveUsername(authentication.getDetails());
    }

    private String resolveUsername(Object source) {
        if (source == null) {
            return null;
        }
        if (source instanceof String value) {
            return value;
        }
        if (source instanceof Map<?, ?> map) {
            Object value = map.get("username");
            if (value instanceof String) {
                return (String) value;
            }
            value = map.get("userName");
            if (value instanceof String) {
                return (String) value;
            }
        }
        try {
            Method method = source.getClass().getMethod("getUsername");
            Object value = method.invoke(source);
            if (value instanceof String) {
                return (String) value;
            }
        } catch (ReflectiveOperationException ex) {
            // ignore
        }
        try {
            Method method = source.getClass().getMethod("getUserName");
            Object value = method.invoke(source);
            if (value instanceof String) {
                return (String) value;
            }
        } catch (ReflectiveOperationException ex) {
            // ignore
        }
        try {
            Method method = source.getClass().getMethod("getName");
            Object value = method.invoke(source);
            if (value instanceof String) {
                return (String) value;
            }
        } catch (ReflectiveOperationException ex) {
            // ignore
        }
        return null;
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public static class ForumListVisibility {
        private final boolean admin;
        private final boolean member;
        private final boolean secretListVisible;
        private final Long userId;

        public ForumListVisibility(boolean admin, boolean member, boolean secretListVisible, Long userId) {
            this.admin = admin;
            this.member = member;
            this.secretListVisible = secretListVisible;
            this.userId = userId;
        }

        public boolean isAdmin() {
            return admin;
        }

        public boolean isMember() {
            return member;
        }

        public boolean isSecretListVisible() {
            return secretListVisible;
        }

        public Long getUserId() {
            return userId;
        }
    }
}
