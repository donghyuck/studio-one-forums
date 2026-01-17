package studio.one.application.forums.persistence.jdbc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import studio.one.platform.data.sqlquery.annotation.SqlMappedStatement;
import studio.one.platform.data.sqlquery.mapping.BoundSql;
import studio.one.platform.data.sqlquery.mapping.MappedStatement;

/**
 * Forums JDBC 영속성 어댑터.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Repository
public class TopicQueryRepositoryImpl implements TopicQueryRepository {
    
    private static final Map<String, String> FIELD_TO_COLUMN = Map.of(
        "id", "t.id",
        "topicId", "t.id",
        "title", "t.title",
        "status", "t.status",
        "createdAt", "t.created_at",
        "updatedAt", "t.updated_at"
    );
    private static final Set<String> SEARCHABLE_FIELDS = Set.of("title", "status");

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @SqlMappedStatement("forums.topicList")
    private MappedStatement topicListStatement;

    public TopicQueryRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TopicListRow> findTopics(Long forumId, String query, Set<String> inFields, Set<String> fields,
                                         Pageable pageable, boolean includeDeleted) {
        String selectClause = buildSelect(fields);
        String searchClause = buildSearchClause(query, inFields);
        String orderClause = buildOrderClause(pageable, "t.updated_at desc");
        Map<String, Object> params = new HashMap<>();
        params.put("forumId", forumId);
        params.put("includeDeleted", includeDeleted);

        if (!searchClause.isBlank()) {
            params.put("query", "%" + query + "%");
        }
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());

        Map<String, Object> dynamicParams = Map.of(
            "selectClause", selectClause,
            "searchClause", searchClause,
            "orderClause", orderClause,
            "includeDeleted", includeDeleted
        );
        BoundSql boundSql = topicListStatement.getBoundSql(params, dynamicParams);
        String sql = boundSql.getSql();

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new TopicListRow(
            rs.getLong("topic_id"),
            rs.getString("title"),
            rs.getString("status"),
            rs.getObject("updated_at", java.time.OffsetDateTime.class)
        ));
    }

    private String buildSelect(Set<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return "t.id as topic_id, t.title, t.status, t.updated_at";
        }
        List<String> selects = new ArrayList<>();
        for (String field : fields) {
            String column = FIELD_TO_COLUMN.get(field);
            if (column != null) {
                String alias = field.equals("topicId") ? "topic_id" : field;
                selects.add(column + " as " + alias);
            }
        }
        if (selects.isEmpty()) {
            return "t.id as topic_id, t.title, t.status, t.updated_at";
        }
        return String.join(", ", selects);
    }

    private String buildSearchClause(String query, Set<String> inFields) {
        if (query == null || query.isBlank() || inFields == null || inFields.isEmpty()) {
            return "";
        }
        List<String> predicates = new ArrayList<>();
        for (String field : inFields) {
            if (!SEARCHABLE_FIELDS.contains(field)) {
                continue;
            }
            String column = FIELD_TO_COLUMN.get(field);
            if (column != null) {
                predicates.add("lower(" + column + ") like lower(:query)");
            }
        }
        if (predicates.isEmpty()) {
            return "";
        }
        return " and (" + String.join(" or ", predicates) + ")";
    }

    private String buildOrderClause(Pageable pageable, String defaultOrderBy) {
        if (!pageable.getSort().isSorted()) {
            return " order by " + defaultOrderBy;
        }
        String orderBy = pageable.getSort().stream()
            .map(order -> FIELD_TO_COLUMN.getOrDefault(order.getProperty(), "t.updated_at") + " " + order.getDirection())
            .collect(Collectors.joining(", "));
        return " order by " + orderBy;
    }
}
