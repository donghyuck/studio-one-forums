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
import studio.one.application.forums.domain.model.Category;
import studio.one.application.forums.domain.repository.CategoryRepository;
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
public class CategoryJdbcRepositoryAdapter implements CategoryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @SqlStatement("forums.categoryInsert")
    private String categoryInsertSql;

    @SqlStatement("forums.categoryUpdate")
    private String categoryUpdateSql;

    @SqlStatement("forums.categorySelectById")
    private String categorySelectByIdSql;

    @SqlStatement("forums.categorySelectByForum")
    private String categorySelectByForumSql;

    @SqlStatement("forums.categoryDelete")
    private String categoryDeleteSql;

    public CategoryJdbcRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Category save(Category category) {
        if (category.id() == null) {
            return insert(category);
        }
        return update(category);
    }

    @Override
    public Optional<Category> findById(Long categoryId) {
        List<Category> rows = jdbcTemplate.query(
            categorySelectByIdSql,
            Map.of("id", categoryId),
            categoryRowMapper
        );
        return rows.stream().findFirst();
    }

    @Override
    public List<Category> findByForumId(Long forumId) {
        return jdbcTemplate.query(
            categorySelectByForumSql,
            Map.of("forumId", forumId),
            categoryRowMapper
        );
    }

    @Override
    public void deleteById(Long categoryId) {
        jdbcTemplate.update(categoryDeleteSql, Map.of("id", categoryId));
    }

    private Category insert(Category category) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("forumId", category.forumId())
            .addValue("name", category.name())
            .addValue("description", category.description())
            .addValue("position", category.position())
            .addValue("createdById", category.createdById())
            .addValue("createdBy", category.createdBy())
            .addValue("createdAt", category.createdAt())
            .addValue("updatedById", category.updatedById())
            .addValue("updatedBy", category.updatedBy())
            .addValue("updatedAt", category.updatedAt())
            .addValue("version", 0L);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(categoryInsertSql, params, keyHolder);
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
        return new Category(
            id,
            category.forumId(),
            category.name(),
            category.description(),
            category.position(),
            category.createdById(),
            category.createdBy(),
            category.createdAt(),
            category.updatedById(),
            category.updatedBy(),
            category.updatedAt()
        );
    }

    private Category update(Category category) {
        Map<String, Object> params = Map.of(
            "id", category.id(),
            "name", category.name(),
            "description", category.description(),
            "position", category.position(),
            "updatedById", category.updatedById(),
            "updatedBy", category.updatedBy(),
            "updatedAt", category.updatedAt()
        );
        jdbcTemplate.update(categoryUpdateSql, params);
        return category;
    }

    private final RowMapper<Category> categoryRowMapper = new RowMapper<>() {
        @Override
        public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Category(
                rs.getLong("id"),
                rs.getLong("forum_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("position"),
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
