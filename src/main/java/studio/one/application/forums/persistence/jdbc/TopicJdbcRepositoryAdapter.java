package studio.one.application.forums.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import studio.one.application.forums.domain.exception.TopicNotFoundException;
import studio.one.application.forums.domain.exception.TopicVersionMismatchException;
import studio.one.application.forums.domain.model.Topic;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.domain.type.TopicStatus;
import studio.one.platform.data.sqlquery.annotation.SqlStatement;

@Repository
public class TopicJdbcRepositoryAdapter implements TopicRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @SqlStatement("forums.topicInsert")
    private String topicInsertSql;

    @SqlStatement("forums.topicUpdate")
    private String topicUpdateSql;

    @SqlStatement("forums.topicSelectById")
    private String topicSelectByIdSql;

    public TopicJdbcRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Topic save(Topic topic) {
        if (topic.id() == null) {
            return insert(topic);
        }
        return update(topic);
    }

    @Override
    public Optional<Topic> findById(Long topicId) {
        List<Topic> rows = jdbcTemplate.query(
            topicSelectByIdSql,
            Map.of("id", topicId),
            topicRowMapper
        );
        return rows.stream().findFirst();
    }

    private Topic insert(Topic topic) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("forumId", topic.forumId())
            .addValue("categoryId", topic.categoryId())
            .addValue("title", topic.title())
            .addValue("tags", joinTags(topic.tags()))
            .addValue("status", topic.status().name())
            .addValue("createdById", topic.createdById())
            .addValue("createdBy", topic.createdBy())
            .addValue("createdAt", topic.createdAt())
            .addValue("updatedById", topic.updatedById())
            .addValue("updatedBy", topic.updatedBy())
            .addValue("updatedAt", topic.updatedAt())
            .addValue("version", topic.version());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(topicInsertSql, params, keyHolder);
        Number key = keyHolder.getKey();
        Long id = key != null ? key.longValue() : null;
        return new Topic(
            id,
            topic.forumId(),
            topic.categoryId(),
            topic.title(),
            topic.tags(),
            topic.status(),
            topic.createdById(),
            topic.createdBy(),
            topic.createdAt(),
            topic.updatedById(),
            topic.updatedBy(),
            topic.updatedAt(),
            topic.version()
        );
    }

    private Topic update(Topic topic) {
        Map<String, Object> params = Map.of(
            "id", topic.id(),
            "title", topic.title(),
            "tags", joinTags(topic.tags()),
            "status", topic.status().name(),
            "updatedById", topic.updatedById(),
            "updatedBy", topic.updatedBy(),
            "updatedAt", topic.updatedAt(),
            "version", topic.version()
        );
        int updated = jdbcTemplate.update(topicUpdateSql, params);
        if (updated == 0) {
            throw TopicVersionMismatchException.byId(topic.id());
        }
        return findById(topic.id())
            .orElseThrow(() -> TopicNotFoundException.byId(topic.id()));
    }

    private String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }

    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .collect(Collectors.toList());
    }

    private final RowMapper<Topic> topicRowMapper = new RowMapper<>() {
        @Override
        public Topic mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Topic(
                rs.getLong("id"),
                rs.getLong("forum_id"),
                rs.getLong("category_id"),
                rs.getString("title"),
                splitTags(rs.getString("tags")),
                TopicStatus.valueOf(rs.getString("status")),
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
