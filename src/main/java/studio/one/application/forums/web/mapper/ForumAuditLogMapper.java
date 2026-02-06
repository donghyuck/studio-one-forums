package studio.one.application.forums.web.mapper;

import studio.one.application.forums.service.audit.query.ForumAuditLogView;
import studio.one.application.forums.web.dto.ForumAuditLogDtos;

/**
 * Forums 감사 로그 웹 매퍼.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-02-05  Son Donghyuck  최초 생성
 * </pre>
 */
public class ForumAuditLogMapper {
    public ForumAuditLogDtos.AuditLogResponse toResponse(ForumAuditLogView view) {
        ForumAuditLogDtos.AuditLogResponse response = new ForumAuditLogDtos.AuditLogResponse();
        response.setAuditId(view.getAuditId());
        response.setForumId(view.getForumId());
        response.setEntityType(view.getEntityType());
        response.setEntityId(view.getEntityId());
        response.setAction(view.getAction());
        response.setActorId(view.getActorId());
        response.setAt(view.getAt());
        response.setDetail(view.getDetail());
        return response;
    }
}
