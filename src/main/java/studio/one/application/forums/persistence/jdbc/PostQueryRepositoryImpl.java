package studio.one.application.forums.persistence.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Forums JDBC 영속성 어댑터.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Repository
public class PostQueryRepositoryImpl implements PostQueryRepository {
    private static final Map<String, String> FIELD_TO_COLUMN = Map.of(
        "id", "p.id",
        "postId", "p.id",
        "createdAt", "p.created_at",
        "updatedAt", "p.updated_at"
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    public PostQueryRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<PostListRow> findPosts(Long topicId, Pageable pageable, boolean includeDeleted, boolean includeHidden) {
        String orderClause = buildOrderClause(pageable, "p.created_at asc");
        Map<String, Object> params = new HashMap<>();
        params.put("topicId", topicId);
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());
        params.put("includeDeleted", includeDeleted);
        params.put("includeHidden", includeHidden);
        String sql = buildSql(orderClause, includeDeleted, includeHidden);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new PostListRow(
            rs.getLong("post_id"),
            rs.getString("content"),
            rs.getLong("created_by_id"),
            rs.getString("created_by"),
            rs.getObject("created_at", java.time.OffsetDateTime.class),
            rs.getLong("version")
        ));
    }

    private String buildSql(String orderClause, boolean includeDeleted, boolean includeHidden) {
        StringBuilder sql = new StringBuilder()
            .append("select p.id as post_id, p.content, p.created_by_id, p.created_by, p.created_at, p.version ")
            .append("from tb_application_posts p ")
            .append("where p.topic_id = :topicId ");
        if (!includeDeleted) {
            sql.append("and p.deleted_at is null ");
        }
        if (!includeHidden) {
            sql.append("and p.hidden_at is null ");
        }
        sql.append(orderClause)
            .append(" limit :limit offset :offset");
        return sql.toString();
    }

    private String buildOrderClause(Pageable pageable, String defaultOrderBy) {
        if (!pageable.getSort().isSorted()) {
            return " order by " + defaultOrderBy;
        }
        String orderBy = pageable.getSort().stream()
            .map(order -> FIELD_TO_COLUMN.getOrDefault(order.getProperty(), "p.created_at") + " " + order.getDirection())
            .collect(Collectors.joining(", "));
        return " order by " + orderBy;
    }
}
