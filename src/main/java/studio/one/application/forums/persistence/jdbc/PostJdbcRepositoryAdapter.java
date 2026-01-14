package studio.one.application.forums.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import studio.one.application.forums.domain.model.Post;
import studio.one.application.forums.domain.repository.PostRepository;
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
            .addValue("version", 0L);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(postInsertSql, params, keyHolder);
        Number key = keyHolder.getKey();
        Long id = key != null ? key.longValue() : null;
        return new Post(
            id,
            post.topicId(),
            post.content(),
            post.createdById(),
            post.createdBy(),
            post.createdAt(),
            post.updatedById(),
            post.updatedBy(),
            post.updatedAt()
        );
    }

    private Post update(Post post) {
        Map<String, Object> params = Map.of(
            "id", post.id(),
            "content", post.content(),
            "updatedById", post.updatedById(),
            "updatedBy", post.updatedBy(),
            "updatedAt", post.updatedAt()
        );
        jdbcTemplate.update(postUpdateSql, params);
        return post;
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
                rs.getObject("updated_at", OffsetDateTime.class)
            );
        }
    };
}
