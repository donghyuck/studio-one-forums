package studio.one.application.forums.web.dto;

import java.time.OffsetDateTime;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Forums 감사 로그 웹 API DTO.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-02-05  Son Donghyuck  최초 생성
 * </pre>
 */
public class ForumAuditLogDtos {
    @Getter
    @Setter
    public static class AuditLogResponse {
        private Long auditId;
        private Long forumId;
        private String entityType;
        private Long entityId;
        private String action;
        private Long actorId;
        private OffsetDateTime at;
        private Map<String, Object> detail;
    }
}
