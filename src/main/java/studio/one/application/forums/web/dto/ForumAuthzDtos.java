package studio.one.application.forums.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTOs that describe what actions are currently allowed for a user.
 */
public class ForumAuthzDtos {
    @Getter
    @Setter
    public static class ActionPermission {
        private String action;
        private boolean allowed;
    }
}
