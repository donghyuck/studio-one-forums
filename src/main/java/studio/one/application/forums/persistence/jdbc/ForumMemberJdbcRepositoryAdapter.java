package studio.one.application.forums.persistence.jdbc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import studio.one.application.forums.domain.model.ForumMember;
import studio.one.application.forums.domain.repository.ForumMemberRepository;
import studio.one.application.forums.domain.type.ForumMemberRole;

@Repository
public class ForumMemberJdbcRepositoryAdapter implements ForumMemberRepository {
    private static final String SELECT_ROLE_SQL = """
        select role
          from tb_application_forum_member
         where forum_id = :forumId
           and user_id = :userId
        """;

    private static final String SELECT_ROLES_BY_USER_SQL = """
        select forum_id, role
          from tb_application_forum_member
         where user_id = :userId
        """;

    private static final String LIST_MEMBERS_SQL = """
        select forum_id, user_id, role, created_by_id, created_at
          from tb_application_forum_member
         where forum_id = :forumId
         order by user_id asc
         limit :limit offset :offset
        """;

    private static final String UPDATE_ROLE_SQL = """
        update tb_application_forum_member
           set role = :role,
               created_by_id = :actorId
         where forum_id = :forumId
           and user_id = :userId
        """;

    private static final String INSERT_MEMBER_SQL = """
        insert into tb_application_forum_member
            (forum_id, user_id, role, created_by_id, created_at)
        values
            (:forumId, :userId, :role, :actorId, :createdAt)
        """;

    private static final String DELETE_MEMBER_SQL = """
        delete from tb_application_forum_member
         where forum_id = :forumId
           and user_id = :userId
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ForumMemberJdbcRepositoryAdapter(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ForumMemberRole> findRole(long forumId, long userId) {
        List<String> roles = jdbcTemplate.query(
            SELECT_ROLE_SQL,
            Map.of("forumId", forumId, "userId", userId),
            (rs, rowNum) -> rs.getString("role")
        );
        return roles.stream().findFirst().map(ForumMemberRole::valueOf);
    }

    @Override
    public Set<ForumMemberRole> findRoles(long forumId, long userId) {
        return findRole(forumId, userId).map(Set::of).orElseGet(Set::of);
    }

    @Override
    public java.util.Map<Long, Set<ForumMemberRole>> findRolesByUserId(long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
            SELECT_ROLES_BY_USER_SQL,
            Map.of("userId", userId)
        );
        if (rows.isEmpty()) {
            return Map.of();
        }
        Map<Long, Set<ForumMemberRole>> result = new java.util.HashMap<>();
        for (Map<String, Object> row : rows) {
            Object forumId = row.get("forum_id");
            Object role = row.get("role");
            if (!(forumId instanceof Number) || role == null) {
                continue;
            }
            long forumIdValue = ((Number) forumId).longValue();
            ForumMemberRole roleValue = ForumMemberRole.valueOf(role.toString());
            result.computeIfAbsent(forumIdValue, key -> new java.util.HashSet<>()).add(roleValue);
        }
        return result;
    }

    @Override
    public List<ForumMember> listMembers(long forumId, int page, int size) {
        int offset = Math.max(page, 0) * Math.max(size, 1);
        return jdbcTemplate.query(
            LIST_MEMBERS_SQL,
            Map.of("forumId", forumId, "limit", size, "offset", offset),
            forumMemberRowMapper
        );
    }

    @Override
    public void upsertMemberRole(long forumId, long userId, ForumMemberRole role, Long actorId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("forumId", forumId)
            .addValue("userId", userId)
            .addValue("role", role.name())
            .addValue("actorId", actorId)
            .addValue("createdAt", OffsetDateTime.now());
        int updated = jdbcTemplate.update(UPDATE_ROLE_SQL, params);
        if (updated == 0) {
            jdbcTemplate.update(INSERT_MEMBER_SQL, params);
        }
    }

    @Override
    public void removeMember(long forumId, long userId) {
        jdbcTemplate.update(DELETE_MEMBER_SQL, Map.of("forumId", forumId, "userId", userId));
    }

    private final RowMapper<ForumMember> forumMemberRowMapper = (rs, rowNum) -> new ForumMember(
        rs.getLong("forum_id"),
        rs.getLong("user_id"),
        ForumMemberRole.valueOf(rs.getString("role")),
        rs.getObject("created_by_id") != null ? rs.getLong("created_by_id") : null,
        rs.getObject("created_at", OffsetDateTime.class)
    );
}
