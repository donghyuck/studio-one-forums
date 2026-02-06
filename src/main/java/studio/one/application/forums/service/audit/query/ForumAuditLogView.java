package studio.one.application.forums.service.audit.query;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Forums 감사 로그 조회 결과.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-02-05  Son Donghyuck  최초 생성
 * </pre>
 */
public class ForumAuditLogView {
    private final Long auditId;
    private final Long forumId;
    private final String entityType;
    private final Long entityId;
    private final String action;
    private final Long actorId;
    private final OffsetDateTime at;
    private final Map<String, Object> detail;

    public ForumAuditLogView(Long auditId, Long forumId, String entityType, Long entityId, String action,
                             Long actorId, OffsetDateTime at, Map<String, Object> detail) {
        this.auditId = auditId;
        this.forumId = forumId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.actorId = actorId;
        this.at = at;
        this.detail = detail;
    }

    public Long getAuditId() {
        return auditId;
    }

    public Long getForumId() {
        return forumId;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getAction() {
        return action;
    }

    public Long getActorId() {
        return actorId;
    }

    public OffsetDateTime getAt() {
        return at;
    }

    public Map<String, Object> getDetail() {
        return detail;
    }
}
