package studio.one.application.forums.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Forums 웹 API DTO.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public class AclDtos {
    @Getter
    @Setter
    public static class PermissionRequest {
        private String sidType;
        private String sid;
        private String permission;
        private Boolean granting;
    }

    @Getter
    @Setter
    public static class AclEntryResponse {
        private String sidType;
        private String sid;
        private String permission;
        private boolean granting;
    }
}
