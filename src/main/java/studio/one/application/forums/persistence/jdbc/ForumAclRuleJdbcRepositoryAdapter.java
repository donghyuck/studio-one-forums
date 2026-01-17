package studio.one.application.forums.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import studio.one.application.forums.domain.acl.Effect;
import studio.one.application.forums.domain.acl.IdentifierType;
import studio.one.application.forums.domain.acl.ForumAclRule;
import studio.one.application.forums.domain.acl.Ownership;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.acl.SubjectType;
import studio.one.application.forums.domain.repository.ForumAclRuleRepository;

@Repository
public class ForumAclRuleJdbcRepositoryAdapter implements ForumAclRuleRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ForumAclRuleJdbcRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ForumAclRule> findRules(long boardId, Long categoryId, PermissionAction action,
                                        Set<String> roleNames, Set<Long> roleIds, Long userId, String username) {
        boolean hasRoleNames = roleNames != null && !roleNames.isEmpty();
        boolean hasRoleIds = roleIds != null && !roleIds.isEmpty();
        boolean hasUserId = userId != null;
        boolean hasUsername = username != null && !username.isBlank();
        if (!hasRoleNames && !hasRoleIds && !hasUserId && !hasUsername) {
            return List.of();
        }
        StringBuilder sql = new StringBuilder()
            .append("select rule_id, board_id, category_id, role, subject_type, identifier_type, subject_id, ")
            .append("subject_name, action, effect, ownership, priority, enabled, ")
            .append("created_by_id, created_at, updated_by_id, updated_at ")
            .append("from tb_forum_acl_rule ")
            .append("where board_id = :boardId ")
            .append("and action = :action ")
            .append("and enabled = true ")
            .append("and (");
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("boardId", boardId)
            .addValue("action", action.name());
        List<String> subjectConditions = new ArrayList<>();
        if (hasRoleNames) {
            subjectConditions.add("(subject_type = 'ROLE' and identifier_type = 'NAME' and subject_name in (:roleNames))");
            params.addValue("roleNames", roleNames);
        }
        if (hasRoleIds) {
            subjectConditions.add("(subject_type = 'ROLE' and identifier_type = 'ID' and subject_id in (:roleIds))");
            params.addValue("roleIds", roleIds);
        }
        if (hasUserId) {
            subjectConditions.add("(subject_type = 'USER' and identifier_type = 'ID' and subject_id = :userId)");
            params.addValue("userId", userId);
        }
        if (hasUsername) {
            subjectConditions.add("(subject_type = 'USER' and identifier_type = 'NAME' and subject_name = :username)");
            params.addValue("username", username);
        }
        sql.append(String.join(" or ", subjectConditions)).append(") ");
        if (categoryId == null) {
            sql.append("and category_id is null ");
        } else {
            sql.append("and (category_id = :categoryId or category_id is null) ");
            params.addValue("categoryId", categoryId);
        }
        return jdbcTemplate.query(sql.toString(), params, forumAclRuleRowMapper);
    }

    private final RowMapper<ForumAclRule> forumAclRuleRowMapper = new RowMapper<>() {
        @Override
        public ForumAclRule mapRow(ResultSet rs, int rowNum) throws SQLException {
            SubjectType subjectType = parseSubjectType(rs.getString("subject_type"));
            IdentifierType identifierType = parseIdentifierType(rs.getString("identifier_type"));
            String subjectName = rs.getString("subject_name");
            if (subjectName == null && identifierType == IdentifierType.NAME) {
                subjectName = rs.getString("role");
            }
            return new ForumAclRule(
                rs.getLong("rule_id"),
                rs.getLong("board_id"),
                rs.getObject("category_id") != null ? rs.getLong("category_id") : null,
                subjectType,
                identifierType,
                rs.getObject("subject_id") != null ? rs.getLong("subject_id") : null,
                subjectName,
                rs.getString("role"),
                PermissionAction.valueOf(rs.getString("action")),
                Effect.valueOf(rs.getString("effect")),
                Ownership.valueOf(rs.getString("ownership")),
                rs.getInt("priority"),
                rs.getBoolean("enabled"),
                rs.getObject("created_by_id") != null ? rs.getLong("created_by_id") : null,
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("updated_by_id") != null ? rs.getLong("updated_by_id") : null,
                rs.getObject("updated_at", OffsetDateTime.class)
            );
        }
    };

    private SubjectType parseSubjectType(String value) {
        if (value == null || value.isBlank()) {
            return SubjectType.ROLE;
        }
        return SubjectType.valueOf(value);
    }

    private IdentifierType parseIdentifierType(String value) {
        if (value == null || value.isBlank()) {
            return IdentifierType.NAME;
        }
        return IdentifierType.valueOf(value);
    }
}
