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
    
    private static final Map<String, String> FIELD_TO_COLUMN = Map.ofEntries(
        Map.entry("id", "t.id"),
        Map.entry("topicId", "t.id"),
        Map.entry("title", "t.title"),
        Map.entry("status", "t.status"),
        Map.entry("createdById", "t.created_by_id"),
        Map.entry("createdBy", "t.created_by"),
        Map.entry("createdAt", "t.created_at"),
        Map.entry("updatedAt", "t.updated_at"),
        Map.entry("postCount", postCountExpr(false)),
        Map.entry("lastPostUpdatedAt", lastPostUpdatedAtExpr(false)),
        Map.entry("lastPostUpdatedById", lastPostUpdatedByIdExpr(false)),
        Map.entry("lastPostUpdatedBy", lastPostUpdatedByExpr(false)),
        Map.entry("lastPostId", lastPostIdExpr(false)),
        Map.entry("lastActivityAt", lastActivityAtExpr(false)),
        Map.entry("excerpt", lastPostExcerptExpr(false))
    );
    private static final Set<String> SEARCHABLE_FIELDS = Set.of("title", "status");

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @SqlMappedStatement("forums.topicList")
    private MappedStatement topicListStatement;

    @SqlMappedStatement("forums.topicCount")
    private MappedStatement topicCountStatement;

    public TopicQueryRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<TopicListRow> findTopics(Long forumId, String query, Set<String> inFields, Set<String> fields,
                                         Pageable pageable, boolean includeDeleted, boolean includeHiddenPosts) {
        String selectClause = buildSelect(fields, includeHiddenPosts);
        String searchClause = buildSearchClause(query, inFields);
        String orderClause = buildOrderClause(pageable, lastActivityOrderExpr(includeHiddenPosts), includeHiddenPosts);
        Map<String, Object> params = new HashMap<>();
        params.put("forumId", forumId);
        params.put("includeDeleted", includeDeleted);
        params.put("selectClause", selectClause);
        params.put("searchClause", searchClause);
        params.put("orderClause", orderClause);
        params.put("includeHiddenPosts", includeHiddenPosts);

        if (!searchClause.isBlank()) {
            params.put("query", "%" + query + "%");
        }
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());

        Map<String, Object> dynamicParams = Map.of(
            "selectClause", selectClause,
            "searchClause", searchClause,
            "orderClause", orderClause,
            "includeDeleted", includeDeleted,
            "includeHiddenPosts", includeHiddenPosts
        );
        BoundSql boundSql = topicListStatement.getBoundSql(params, dynamicParams);
        String sql = boundSql.getSql();

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new TopicListRow(
            rs.getLong("topic_id"),
            rs.getString("title"),
            rs.getString("status"),
            rs.getObject("updated_at", java.time.OffsetDateTime.class),
            rs.getObject("created_by_id", Long.class),
            rs.getString("created_by"),
            rs.getLong("post_count"),
            rs.getObject("last_post_updated_at", java.time.OffsetDateTime.class),
            rs.getObject("last_post_updated_by_id", Long.class),
            rs.getString("last_post_updated_by"),
            rs.getObject("last_post_id", Long.class),
            rs.getObject("last_activity_at", java.time.OffsetDateTime.class),
            rs.getString("excerpt")
        ));
    }

    @Override
    public long countTopics(Long forumId, String query, Set<String> inFields, boolean includeDeleted) {
        String searchClause = buildSearchClause(query, inFields);
        Map<String, Object> params = new HashMap<>();
        params.put("forumId", forumId);
        params.put("includeDeleted", includeDeleted);
        params.put("searchClause", searchClause);
        if (!searchClause.isBlank()) {
            params.put("query", "%" + query + "%");
        }
        Map<String, Object> dynamicParams = Map.of(
            "searchClause", searchClause,
            "includeDeleted", includeDeleted
        );
        BoundSql boundSql = topicCountStatement.getBoundSql(params, dynamicParams);
        Long total = jdbcTemplate.queryForObject(boundSql.getSql(), params, Long.class);
        return total != null ? total : 0L;
    }

    private String buildSelect(Set<String> fields, boolean includeHiddenPosts) {
        List<String> selects = new ArrayList<>();
        selects.add("t.id as topic_id");
        selects.add("t.title");
        selects.add("t.status");
        selects.add("t.updated_at");
        selects.add("t.created_by_id");
        selects.add("t.created_by");
        selects.add(postCountExpr(includeHiddenPosts) + " as post_count");
        selects.add(lastPostUpdatedAtExpr(includeHiddenPosts) + " as last_post_updated_at");
        selects.add(lastPostUpdatedByIdExpr(includeHiddenPosts) + " as last_post_updated_by_id");
        selects.add(lastPostUpdatedByExpr(includeHiddenPosts) + " as last_post_updated_by");
        selects.add(lastPostIdExpr(includeHiddenPosts) + " as last_post_id");
        selects.add(lastActivityAtExpr(includeHiddenPosts) + " as last_activity_at");
        selects.add(lastPostExcerptExpr(includeHiddenPosts) + " as excerpt");
        if (fields == null || fields.isEmpty()) {
            return String.join(", ", selects);
        }
        for (String field : fields) {
            String column = FIELD_TO_COLUMN.get(field);
            if (column != null) {
                String alias = field.equals("topicId") ? "topic_id" : field;
                String select = column + " as " + alias;
                if (!selects.contains(select)) {
                    selects.add(select);
                }
            }
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

    private String buildOrderClause(Pageable pageable, String defaultOrderBy, boolean includeHiddenPosts) {
        if (!pageable.getSort().isSorted()) {
            return " order by " + defaultOrderBy;
        }
        String orderBy = pageable.getSort().stream()
            .map(order -> orderColumn(order.getProperty(), includeHiddenPosts) + " " + order.getDirection())
            .collect(Collectors.joining(", "));
        return " order by " + orderBy;
    }

    private String orderColumn(String property, boolean includeHiddenPosts) {
        return switch (property) {
            case "postCount" -> postCountExpr(includeHiddenPosts);
            case "lastPostUpdatedAt" -> lastPostUpdatedAtExpr(includeHiddenPosts);
            case "lastPostUpdatedById" -> lastPostUpdatedByIdExpr(includeHiddenPosts);
            case "lastPostUpdatedBy" -> lastPostUpdatedByExpr(includeHiddenPosts);
            case "lastPostId" -> lastPostIdExpr(includeHiddenPosts);
            case "lastActivityAt" -> lastActivityAtExpr(includeHiddenPosts);
            case "excerpt" -> lastPostExcerptExpr(includeHiddenPosts);
            default -> FIELD_TO_COLUMN.getOrDefault(property, "t.updated_at");
        };
    }

    private static String hiddenClause(boolean includeHiddenPosts) {
        return includeHiddenPosts ? "" : " and p.hidden_at is null";
    }

    private static String postCountExpr(boolean includeHiddenPosts) {
        return "(select count(1) from tb_application_posts p"
            + " where p.topic_id = t.id and p.deleted_at is null"
            + hiddenClause(includeHiddenPosts) + ")";
    }

    private static String lastPostUpdatedAtExpr(boolean includeHiddenPosts) {
        return "(select p.updated_at from tb_application_posts p"
            + " where p.topic_id = t.id and p.deleted_at is null"
            + hiddenClause(includeHiddenPosts)
            + " order by p.updated_at desc, p.id desc limit 1)";
    }

    private static String lastPostUpdatedByIdExpr(boolean includeHiddenPosts) {
        return "(select p.updated_by_id from tb_application_posts p"
            + " where p.topic_id = t.id and p.deleted_at is null"
            + hiddenClause(includeHiddenPosts)
            + " order by p.updated_at desc, p.id desc limit 1)";
    }

    private static String lastPostUpdatedByExpr(boolean includeHiddenPosts) {
        return "(select p.updated_by from tb_application_posts p"
            + " where p.topic_id = t.id and p.deleted_at is null"
            + hiddenClause(includeHiddenPosts)
            + " order by p.updated_at desc, p.id desc limit 1)";
    }

    private static String lastPostIdExpr(boolean includeHiddenPosts) {
        return "(select p.id from tb_application_posts p"
            + " where p.topic_id = t.id and p.deleted_at is null"
            + hiddenClause(includeHiddenPosts)
            + " order by p.updated_at desc, p.id desc limit 1)";
    }

    private static String lastPostExcerptExpr(boolean includeHiddenPosts) {
        return "(select substring(p.content, 1, 200) from tb_application_posts p"
            + " where p.topic_id = t.id and p.deleted_at is null"
            + hiddenClause(includeHiddenPosts)
            + " order by p.updated_at desc, p.id desc limit 1)";
    }

    private static String lastActivityAtExpr(boolean includeHiddenPosts) {
        return "coalesce(" + lastPostUpdatedAtExpr(includeHiddenPosts) + ", t.updated_at)";
    }

    private static String lastActivityOrderExpr(boolean includeHiddenPosts) {
        return lastActivityAtExpr(includeHiddenPosts) + " desc, t.id desc";
    }
}
