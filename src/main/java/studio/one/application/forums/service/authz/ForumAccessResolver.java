package studio.one.application.forums.service.authz;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import studio.one.application.forums.domain.repository.ForumMemberRepository;
import studio.one.application.forums.domain.type.ForumMemberRole;

@Component
public class ForumAccessResolver {
    private final ForumMemberRepository forumMemberRepository;
    private final Set<String> adminRoles;
    private final ObjectProvider<ForumAuthzRequestCache> cacheProvider;

    public ForumAccessResolver(ForumMemberRepository forumMemberRepository,
                               @Value("${studio.features.forums.authz.admin-roles:ROLE_ADMIN,ADMIN}") Collection<String> adminRoles,
                               ObjectProvider<ForumAuthzRequestCache> cacheProvider) {
        this.forumMemberRepository = forumMemberRepository;
        this.adminRoles = normalizeRoles(adminRoles);
        this.cacheProvider = cacheProvider;
    }

    public ForumAccessResolver(ForumMemberRepository forumMemberRepository,
                               Collection<String> adminRoles) {
        this(forumMemberRepository, adminRoles, emptyProvider());
    }

    private static ObjectProvider<ForumAuthzRequestCache> emptyProvider() {
        return new ObjectProvider<>() {
            @Override
            public ForumAuthzRequestCache getObject() {
                return null;
            }

            @Override
            public ForumAuthzRequestCache getObject(Object... args) {
                return null;
            }

            @Override
            public ForumAuthzRequestCache getIfAvailable() {
                return null;
            }

            @Override
            public ForumAuthzRequestCache getIfUnique() {
                return null;
            }

            @Override
            public java.util.stream.Stream<ForumAuthzRequestCache> stream() {
                return java.util.stream.Stream.empty();
            }

            @Override
            public java.util.stream.Stream<ForumAuthzRequestCache> orderedStream() {
                return java.util.stream.Stream.empty();
            }
        };
    }

    public ForumAccessContext resolveContext() {
        Set<String> roles = resolveRoles();
        Long userId = resolveUserId();
        String username = resolveUsername();
        return new ForumAccessContext(roles, userId, username);
    }

    public boolean isAdmin(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        if (adminRoles.isEmpty()) {
            return false;
        }
        return roles.stream().anyMatch(role -> adminRoles.contains(role) || "MODERATOR".equals(role));
    }

    public Map<Long, Set<ForumMemberRole>> resolveMemberRoles(Long userId) {
        if (userId == null) {
            return Map.of();
        }
        ForumAuthzRequestCache cache = cacheProvider.getIfAvailable();
        if (cache == null) {
            return forumMemberRepository.findRolesByUserId(userId);
        }
        return cache.getMemberRoles(userId, () -> forumMemberRepository.findRolesByUserId(userId));
    }

    public Set<String> effectiveRoles(Set<String> baseRoles, Set<ForumMemberRole> memberRoles) {
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

    public Set<String> effectiveRoles(Long forumId, Long userId, Set<String> baseRoles) {
        Set<String> roles = baseRoles == null ? Set.of() : baseRoles;
        if (forumId == null || userId == null) {
            return roles;
        }
        Set<ForumMemberRole> memberRoles = null;
        ForumAuthzRequestCache cache = cacheProvider.getIfAvailable();
        if (cache != null) {
            Map<Long, Set<ForumMemberRole>> cached = cache.getMemberRoles(userId, () -> forumMemberRepository.findRolesByUserId(userId));
            memberRoles = cached.get(forumId);
        }
        if (memberRoles == null) {
            memberRoles = forumMemberRepository.findRoles(forumId, userId);
        }
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
}
