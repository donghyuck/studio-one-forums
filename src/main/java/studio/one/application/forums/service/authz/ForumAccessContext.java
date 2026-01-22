package studio.one.application.forums.service.authz;

import java.util.Set;

public class ForumAccessContext {
    private final Set<String> roles;
    private final Long userId;
    private final String username;

    public ForumAccessContext(Set<String> roles, Long userId, String username) {
        this.roles = roles;
        this.userId = userId;
        this.username = username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public boolean isMember() {
        return userId != null;
    }
}
