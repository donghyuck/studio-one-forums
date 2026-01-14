package studio.one.application.forums.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.exception.ForumVersionMismatchException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.vo.ForumSlug;
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
public class ForumJdbcRepositoryAdapter implements ForumRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @SqlStatement("forums.forumInsert")
    private String forumInsertSql;

    @SqlStatement("forums.forumUpdate")
    private String forumUpdateSql;

    @SqlStatement("forums.forumSelectById")
    private String forumSelectByIdSql;

    @SqlStatement("forums.forumSelectBySlug")
    private String forumSelectBySlugSql;

    @SqlStatement("forums.forumSelectAll")
    private String forumSelectAllSql;

    @SqlStatement("forums.forumExistsBySlug")
    private String forumExistsBySlugSql;

    public ForumJdbcRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Forum save(Forum forum) {
        if (forum.id() == null) {
            return insert(forum);
        }
        return update(forum);
    }

    @Override
    public Optional<Forum> findById(Long forumId) {
        List<Forum> rows = jdbcTemplate.query(
            forumSelectByIdSql,
            Map.of("id", forumId),
            forumRowMapper
        );
        return rows.stream().findFirst();
    }

    @Override
    public Optional<Forum> findBySlug(ForumSlug slug) {
        List<Forum> rows = jdbcTemplate.query(
            forumSelectBySlugSql,
            Map.of("slug", slug.value()),
            forumRowMapper
        );
        return rows.stream().findFirst();
    }

    @Override
    public boolean existsBySlug(ForumSlug slug) {
        Integer count = jdbcTemplate.queryForObject(
            forumExistsBySlugSql,
            Map.of("slug", slug.value()),
            Integer.class
        );
        return count != null && count > 0;
    }

    @Override
    public List<Forum> findAll() {
        return jdbcTemplate.query(forumSelectAllSql, Collections.emptyMap(), forumRowMapper);
    }

    private Forum insert(Forum forum) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("slug", forum.slug().value())
            .addValue("name", forum.name())
            .addValue("description", forum.description())
            .addValue("createdById", forum.createdById())
            .addValue("createdBy", forum.createdBy())
            .addValue("createdAt", forum.createdAt())
            .addValue("updatedById", forum.updatedById())
            .addValue("updatedBy", forum.updatedBy())
            .addValue("updatedAt", forum.updatedAt())
            .addValue("version", forum.version());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(forumInsertSql, params, keyHolder);
        Number key = keyHolder.getKey();
        Long id = key != null ? key.longValue() : null;
        return new Forum(
            id,
            forum.slug(),
            forum.name(),
            forum.description(),
            forum.createdById(),
            forum.createdBy(),
            forum.createdAt(),
            forum.updatedById(),
            forum.updatedBy(),
            forum.updatedAt(),
            forum.version()
        );
    }

    private Forum update(Forum forum) {
        Map<String, Object> params = Map.of(
            "id", forum.id(),
            "name", forum.name(),
            "description", forum.description(),
            "updatedById", forum.updatedById(),
            "updatedBy", forum.updatedBy(),
            "updatedAt", forum.updatedAt(),
            "version", forum.version()
        );
        int updated = jdbcTemplate.update(forumUpdateSql, params);
        if (updated == 0) {
            throw ForumVersionMismatchException.bySlug(forum.slug().value());
        }
        return findById(forum.id())
            .orElseThrow(() -> ForumNotFoundException.bySlug(forum.slug().value()));
    }

    private final RowMapper<Forum> forumRowMapper = new RowMapper<>() {
        @Override
        public Forum mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Forum(
                rs.getLong("id"),
                ForumSlug.of(rs.getString("slug")),
                rs.getString("name"),
                rs.getString("description"),
                rs.getLong("created_by_id"),
                rs.getString("created_by"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getLong("updated_by_id"),
                rs.getString("updated_by"),
                rs.getObject("updated_at", OffsetDateTime.class),
                rs.getLong("version")
            );
        }
    };
}
