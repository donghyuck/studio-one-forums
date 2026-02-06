package studio.one.application.forums.service.audit.query;

import java.time.OffsetDateTime;

/**
 * Forums 감사 로그 조회 조건.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-02-05  Son Donghyuck  최초 생성
 * </pre>
 */
public class ForumAuditLogQuery {
    private final Long forumId;
    private final String entityType;
    private final Long entityId;
    private final Long actorId;
    private final OffsetDateTime from;
    private final OffsetDateTime to;

    public ForumAuditLogQuery(Long forumId, String entityType, Long entityId, Long actorId,
                              OffsetDateTime from, OffsetDateTime to) {
        this.forumId = forumId;
        this.entityType = normalize(entityType);
        this.entityId = entityId;
        this.actorId = actorId;
        this.from = from;
        this.to = to;
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

    public Long getActorId() {
        return actorId;
    }

    public OffsetDateTime getFrom() {
        return from;
    }

    public OffsetDateTime getTo() {
        return to;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }
}
