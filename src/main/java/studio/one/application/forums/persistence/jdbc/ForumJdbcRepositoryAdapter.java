package studio.one.application.forums.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.exception.ForumVersionMismatchException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.platform.data.sqlquery.mapping.BoundSql;
import studio.one.platform.data.sqlquery.mapping.MappedStatement;
import studio.one.platform.data.sqlquery.annotation.SqlMappedStatement;
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

    @SqlStatement("forums.forumPropertySelectByForumId")
    private String forumPropertySelectByForumIdSql;

    @SqlStatement("forums.forumPropertyDeleteByForumId")
    private String forumPropertyDeleteByForumIdSql;

    @SqlStatement("forums.forumPropertyInsert")
    private String forumPropertyInsertSql;

    @SqlMappedStatement("forums.forumList")
    private MappedStatement forumListStatement;

    @SqlMappedStatement("forums.forumListVisible")
    private MappedStatement forumListVisibleStatement;

    @SqlMappedStatement("forums.forumCount")
    private MappedStatement forumCountStatement;

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

    @Override
    public Page<Forum> search(String query, Set<String> inFields, Pageable pageable) {
        Set<String> fields = normalizeInFields(inFields);
        String searchClause = buildSearchClause(query, fields);
        String orderClause = buildOrderClause(pageable, "f.updated_at desc, f.id desc");
        Map<String, Object> params = new java.util.HashMap<>();
        if (!searchClause.isBlank()) {
            params.put("query", "%" + query + "%");
        }
        params.put("limit", pageable.getPageSize());
        params.put("offset", pageable.getOffset());

        BoundSql countSql = forumCountStatement.getBoundSql(params, Map.of("searchClause", searchClause));
        Long total = jdbcTemplate.queryForObject(countSql.getSql(), params, Long.class);
        if (total == null || total == 0L) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        BoundSql listSql = forumListStatement.getBoundSql(params, Map.of(
            "searchClause", searchClause,
            "orderClause", orderClause
        ));
        List<Forum> rows = jdbcTemplate.query(listSql.getSql(), params, forumRowMapper);
        return new PageImpl<>(rows, pageable, total);
    }

    @Override
    public List<Forum> searchCandidates(String query, Set<String> inFields, boolean isAdmin,
                                        boolean isMember, boolean secretListVisible, Long userId) {
        Set<String> fields = normalizeInFields(inFields);
        String searchClause = buildSearchClause(query, fields);
        String orderClause = buildOrderClause(Pageable.unpaged(), "f.updated_at desc, f.id desc");
        String visibilityClause = buildVisibilityClause(isAdmin, isMember, secretListVisible, userId);
        Map<String, Object> params = new java.util.HashMap<>();
        if (!searchClause.isBlank()) {
            params.put("query", "%" + query + "%");
        }
        params.put("userId", userId);

        BoundSql listSql = forumListVisibleStatement.getBoundSql(params, Map.of(
            "searchClause", searchClause,
            "orderClause", orderClause,
            "visibilityClause", visibilityClause
        ));
        return jdbcTemplate.query(listSql.getSql(), params, forumRowMapper);
    }

    private Forum insert(Forum forum) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("slug", forum.slug().value())
            .addValue("name", forum.name())
            .addValue("description", forum.description())
            .addValue("type", (forum.type() != null ? forum.type() : ForumType.COMMON).name())
            .addValue("createdById", forum.createdById())
            .addValue("createdBy", forum.createdBy())
            .addValue("createdAt", forum.createdAt())
            .addValue("updatedById", forum.updatedById())
            .addValue("updatedBy", forum.updatedBy())
            .addValue("updatedAt", forum.updatedAt())
            .addValue("version", forum.version());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(forumInsertSql, params, keyHolder);
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
        if (id != null) {
            saveProperties(id, forum.properties());
        }
        return new Forum(
            id,
            forum.slug(),
            forum.name(),
            forum.description(),
            forum.type() != null ? forum.type() : ForumType.COMMON,
            forum.properties(),
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
        saveProperties(forum.id(), forum.properties());
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
                ForumType.valueOf(rs.getString("type")),
                loadProperties(rs.getLong("id")),
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

    private Map<String, String> loadProperties(Long forumId) {
        if (forumId == null) {
            return Map.of();
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            forumPropertySelectByForumIdSql,
            Map.of("forumId", forumId)
        );
        if (rows.isEmpty()) {
            return Map.of();
        }
        Map<String, String> properties = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object name = row.get("property_name");
            Object value = row.get("property_value");
            if (name != null) {
                properties.put(name.toString(), value != null ? value.toString() : null);
            }
        }
        return properties;
    }

    private void saveProperties(Long forumId, Map<String, String> properties) {
        if (forumId == null) {
            return;
        }
        jdbcTemplate.update(forumPropertyDeleteByForumIdSql, Map.of("forumId", forumId));
        if (properties == null || properties.isEmpty()) {
            return;
        }
        MapSqlParameterSource[] batch = properties.entrySet().stream()
            .map(entry -> new MapSqlParameterSource()
                .addValue("forumId", forumId)
                .addValue("propertyName", entry.getKey())
                .addValue("propertyValue", entry.getValue()))
            .toArray(MapSqlParameterSource[]::new);
        jdbcTemplate.batchUpdate(forumPropertyInsertSql, batch);
    }

    private Set<String> normalizeInFields(Set<String> inFields) {
        if (inFields == null || inFields.isEmpty()) {
            return Set.of("slug", "name", "description");
        }
        return inFields.stream()
            .filter(field -> field.equals("slug") || field.equals("name") || field.equals("description"))
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private String buildSearchClause(String query, Set<String> inFields) {
        if (query == null || query.isBlank() || inFields == null || inFields.isEmpty()) {
            return "";
        }
        List<String> predicates = new ArrayList<>();
        for (String field : inFields) {
            String column = switch (field) {
                case "slug" -> "f.slug";
                case "name" -> "f.name";
                case "description" -> "f.description";
                default -> null;
            };
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
            .map(order -> mapSortProperty(order.getProperty()) + " " + order.getDirection())
            .collect(java.util.stream.Collectors.joining(", "));
        return " order by " + orderBy;
    }

    private String buildVisibilityClause(boolean isAdmin, boolean isMember, boolean secretListVisible, Long userId) {
        if (isAdmin) {
            return "";
        }
        List<String> predicates = new ArrayList<>();
        predicates.add("f.type in ('COMMON','NOTICE')");
        if (isMember) {
            predicates.add("f.type = 'SECRET'");
        }
        if (userId != null) {
            String memberAdminExists = "exists (select 1 from tb_application_forum_member m"
                + " where m.forum_id = f.id and m.user_id = :userId"
                + " and m.role in ('OWNER','ADMIN','MODERATOR'))";
            predicates.add("(f.type = 'ADMIN_ONLY' and " + memberAdminExists + ")");
            if (!isMember) {
                predicates.add("(f.type = 'SECRET' and " + memberAdminExists + ")");
            }
        }
        if (predicates.isEmpty()) {
            return " and 1 = 0";
        }
        return " and (" + String.join(" or ", predicates) + ")";
    }

    private String mapSortProperty(String property) {
        return switch (property) {
            case "slug" -> "f.slug";
            case "name" -> "f.name";
            case "description" -> "f.description";
            case "updatedAt" -> "f.updated_at";
            case "createdAt" -> "f.created_at";
            default -> "f.updated_at";
        };
    }
}
