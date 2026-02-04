package studio.one.application.forums.web.dto;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import studio.one.application.forums.domain.acl.PermissionAction;

public class PermissionActionDtos {

    @Getter
    @Setter
    public static class ActionResponse {
        private String action;
        private String description;
        private String displayName;

        public static ActionResponse of(PermissionAction action) {
            ActionResponse dto = new ActionResponse();
            dto.action = action.name();
            dto.description = action.description();
            dto.displayName = action.name().toLowerCase().replace('_', ' ');
            return dto;
        }
    }

    public static List<ActionResponse> all() {
        return List.of(PermissionAction.values()).stream()
            .map(ActionResponse::of)
            .collect(Collectors.toList());
    }
}
