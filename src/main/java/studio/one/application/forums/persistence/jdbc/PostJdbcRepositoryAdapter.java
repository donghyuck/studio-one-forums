package studio.one.application.forums.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import studio.one.application.forums.domain.model.Post;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.domain.exception.PostVersionMismatchException;
import studio.one.platform.data.sqlquery.annotation.SqlStatement;

/**
 * Forums JDBC 영속성 어댑터.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Repository
public class PostJdbcRepositoryAdapter implements PostRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @SqlStatement("forums.postInsert")
    private String postInsertSql;

    @SqlStatement("forums.postUpdate")
    private String postUpdateSql;

    @SqlStatement("forums.postSelectByTopic")
    private String postSelectByTopicSql;

    @SqlStatement("forums.postSelectById")
    private String postSelectByIdSql;

    public PostJdbcRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Post save(Post post) {
        if (post.id() == null) {
            return insert(post);
        }
        return update(post);
    }

    @Override
    public List<Post> findByTopicId(Long topicId) {
        return jdbcTemplate.query(
            postSelectByTopicSql,
            Map.of("topicId", topicId),
            postRowMapper
        );
    }

    @Override
    public Optional<Post> findById(Long postId) {
        List<Post> rows = jdbcTemplate.query(
            postSelectByIdSql,
            Map.of("postId", postId),
            postRowMapper
        );
        return rows.stream().findFirst();
    }

    private Post insert(Post post) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("topicId", post.topicId())
            .addValue("content", post.content())
            .addValue("createdById", post.createdById())
            .addValue("createdBy", post.createdBy())
            .addValue("createdAt", post.createdAt())
            .addValue("updatedById", post.updatedById())
            .addValue("updatedBy", post.updatedBy())
            .addValue("updatedAt", post.updatedAt())
            .addValue("deletedAt", post.deletedAt())
            .addValue("deletedById", post.deletedById())
            .addValue("hiddenAt", post.hiddenAt())
            .addValue("hiddenById", post.hiddenById())
            .addValue("version", 0L);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(postInsertSql, params, keyHolder);
        Long id = null;
        Map<String, Object> keys = keyHolder.getKeys();
        if (keys != null) {
            Object value = keys.get("id");
            if (value instanceof Number) {
                id = ((Number) value).longValue();
            }
        }
        if (id == null) {
            Number key = keyHolder.getKey();
            id = key != null ? key.longValue() : null;
        }
        return new Post(
            id,
            post.topicId(),
            post.content(),
            post.createdById(),
            post.createdBy(),
            post.createdAt(),
            post.updatedById(),
            post.updatedBy(),
            post.updatedAt(),
            post.deletedAt(),
            post.deletedById(),
            post.hiddenAt(),
            post.hiddenById(),
            0L
        );
    }

    private Post update(Post post) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", post.id())
            .addValue("content", post.content())
            .addValue("updatedById", post.updatedById())
            .addValue("updatedBy", post.updatedBy())
            .addValue("updatedAt", post.updatedAt())
            .addValue("deletedAt", post.deletedAt())
            .addValue("deletedById", post.deletedById())
            .addValue("hiddenAt", post.hiddenAt())
            .addValue("hiddenById", post.hiddenById())
            .addValue("version", post.version());
        int updated = jdbcTemplate.update(postUpdateSql, params);
        if (updated == 0) {
            throw PostVersionMismatchException.byId(post.id());
        }
        return new Post(
            post.id(),
            post.topicId(),
            post.content(),
            post.createdById(),
            post.createdBy(),
            post.createdAt(),
            post.updatedById(),
            post.updatedBy(),
            post.updatedAt(),
            post.deletedAt(),
            post.deletedById(),
            post.hiddenAt(),
            post.hiddenById(),
            post.version() + 1
        );
    }

    private final RowMapper<Post> postRowMapper = new RowMapper<>() {
        @Override
        public Post mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Post(
                rs.getLong("id"),
                rs.getLong("topic_id"),
                rs.getString("content"),
                rs.getLong("created_by_id"),
                rs.getString("created_by"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getLong("updated_by_id"),
                rs.getString("updated_by"),
                rs.getObject("updated_at", OffsetDateTime.class),
                rs.getObject("deleted_at", OffsetDateTime.class),
                rs.getObject("deleted_by_id") != null ? rs.getLong("deleted_by_id") : null,
                rs.getObject("hidden_at", OffsetDateTime.class),
                rs.getObject("hidden_by_id") != null ? rs.getLong("hidden_by_id") : null,
                rs.getLong("version")
            );
        }
    };
}
