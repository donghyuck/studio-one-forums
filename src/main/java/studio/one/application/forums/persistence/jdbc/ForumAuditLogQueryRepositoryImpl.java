package studio.one.application.forums.persistence.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import studio.one.application.forums.service.audit.query.ForumAuditLogQuery;

/**
 * Forums 감사 로그 JDBC 조회.
 */
@Repository
public class ForumAuditLogQueryRepositoryImpl implements ForumAuditLogQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ForumAuditLogQueryRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Page<ForumAuditLogRow> find(ForumAuditLogQuery query, Pageable pageable) {
        String whereClause = buildWhereClause(query);
        String orderClause = buildOrderClause(pageable, "a.at desc, a.audit_id desc");
        MapSqlParameterSource params = buildParams(query, pageable);

        String countSql = "select count(1) from tb_application_forum_audit_log a where 1 = 1" + whereClause;
        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        if (total == null || total == 0L) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        String listSql = "select a.audit_id, a.board_id, a.entity_type, a.entity_id, a.action,"
            + " a.actor_id, a.at, a.detail"
            + " from tb_application_forum_audit_log a"
            + " where 1 = 1"
            + whereClause
            + orderClause
            + " limit :limit offset :offset";
        List<ForumAuditLogRow> rows = jdbcTemplate.query(listSql, params, (rs, rowNum) -> new ForumAuditLogRow(
            rs.getObject("audit_id", Long.class),
            rs.getObject("board_id", Long.class),
            rs.getString("entity_type"),
            rs.getObject("entity_id", Long.class),
            rs.getString("action"),
            rs.getObject("actor_id", Long.class),
            rs.getObject("at", java.time.OffsetDateTime.class),
            rs.getString("detail")
        ));
        return new PageImpl<>(rows, pageable, total);
    }

    private String buildWhereClause(ForumAuditLogQuery query) {
        if (query == null) {
            return "";
        }
        List<String> clauses = new ArrayList<>();
        if (query.getForumId() != null) {
            clauses.add("a.board_id = :forumId");
        }
        if (query.getEntityType() != null) {
            clauses.add("a.entity_type = :entityType");
        }
        if (query.getEntityId() != null) {
            clauses.add("a.entity_id = :entityId");
        }
        if (query.getActorId() != null) {
            clauses.add("a.actor_id = :actorId");
        }
        if (query.getFrom() != null) {
            clauses.add("a.at >= :from");
        }
        if (query.getTo() != null) {
            clauses.add("a.at < :to");
        }
        if (clauses.isEmpty()) {
            return "";
        }
        return " and " + String.join(" and ", clauses);
    }

    private MapSqlParameterSource buildParams(ForumAuditLogQuery query, Pageable pageable) {
        Map<String, Object> params = new HashMap<>();
        if (query != null) {
            if (query.getForumId() != null) {
                params.put("forumId", query.getForumId());
            }
            if (query.getEntityType() != null) {
                params.put("entityType", query.getEntityType());
            }
            if (query.getEntityId() != null) {
                params.put("entityId", query.getEntityId());
            }
            if (query.getActorId() != null) {
                params.put("actorId", query.getActorId());
            }
            if (query.getFrom() != null) {
                params.put("from", query.getFrom());
            }
            if (query.getTo() != null) {
                params.put("to", query.getTo());
            }
        }
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());
        return new MapSqlParameterSource(params);
    }

    private String buildOrderClause(Pageable pageable, String defaultOrderBy) {
        if (!pageable.getSort().isSorted()) {
            return " order by " + defaultOrderBy;
        }
        String orderBy = pageable.getSort().stream()
            .map(order -> mapSortProperty(order.getProperty()) + " " + order.getDirection())
            .collect(java.util.stream.Collectors.joining(", "));
        return " order by " + orderBy;
    }

    private String mapSortProperty(String property) {
        return switch (property) {
            case "auditId" -> "a.audit_id";
            case "forumId" -> "a.board_id";
            case "entityType" -> "a.entity_type";
            case "entityId" -> "a.entity_id";
            case "action" -> "a.action";
            case "actorId" -> "a.actor_id";
            case "at" -> "a.at";
            default -> "a.at";
        };
    }
}
