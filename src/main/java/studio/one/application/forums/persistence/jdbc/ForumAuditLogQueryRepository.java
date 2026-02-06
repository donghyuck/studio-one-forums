package studio.one.application.forums.persistence.jdbc;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studio.one.application.forums.service.audit.query.ForumAuditLogQuery;

/**
 * Forums 감사 로그 조회.
 */
public interface ForumAuditLogQueryRepository {
    Page<ForumAuditLogRow> find(ForumAuditLogQuery query, Pageable pageable);
}
