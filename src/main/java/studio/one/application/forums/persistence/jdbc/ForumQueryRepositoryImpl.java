package studio.one.application.forums.persistence.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ForumQueryRepositoryImpl implements ForumQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ForumQueryRepositoryImpl(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<Long, ForumSummaryMetricsRow> findForumSummaries(List<Long> forumIds, boolean includeHiddenPosts) {
        if (forumIds == null || forumIds.isEmpty()) {
            return Map.of();
        }
        String hiddenClause = includeHiddenPosts ? "" : " and p.hidden_at is null";
        String sql = """
            with activity as (
                select t.forum_id as forum_id,
                       t.updated_at as activity_at,
                       t.updated_by_id as activity_by_id,
                       t.updated_by as activity_by,
                       'TOPIC' as activity_type,
                       t.id as activity_id
                  from tb_application_topics t
                 where t.deleted_at is null
                union all
                select t.forum_id as forum_id,
                       p.updated_at as activity_at,
                       p.updated_by_id as activity_by_id,
                       p.updated_by as activity_by,
                       'POST' as activity_type,
                       p.id as activity_id
                  from tb_application_posts p
                  join tb_application_topics t on t.id = p.topic_id
                 where p.deleted_at is null
            """
            + hiddenClause + """
                   and t.deleted_at is null
            ),
            ranked as (
                select a.*,
                       row_number() over (partition by a.forum_id order by a.activity_at desc, a.activity_id desc) as rn
                  from activity a
            )
            select f.id as forum_id,
                   count(distinct t.id) as topic_count,
                   count(distinct p.id) as post_count,
                   r.activity_at as last_activity_at,
                   r.activity_by_id as last_activity_by_id,
                   r.activity_by as last_activity_by,
                   r.activity_type as last_activity_type,
                   r.activity_id as last_activity_id
              from tb_application_forums f
              left join tb_application_topics t
                on t.forum_id = f.id
               and t.deleted_at is null
              left join tb_application_posts p
                on p.topic_id = t.id
               and p.deleted_at is null
            """
            + hiddenClause + """
              left join ranked r
                on r.forum_id = f.id
               and r.rn = 1
             where f.id in (:forumIds)
             group by f.id, r.activity_at, r.activity_by_id, r.activity_by, r.activity_type, r.activity_id
            """;

        Map<String, Object> params = Map.of("forumIds", forumIds);
        Map<Long, ForumSummaryMetricsRow> results = new HashMap<>();
        jdbcTemplate.query(sql, params, rs -> {
            Long forumId = rs.getLong("forum_id");
            ForumSummaryMetricsRow row = new ForumSummaryMetricsRow(
                forumId,
                rs.getLong("topic_count"),
                rs.getLong("post_count"),
                rs.getObject("last_activity_at", java.time.OffsetDateTime.class),
                rs.getObject("last_activity_by_id", Long.class),
                rs.getString("last_activity_by"),
                rs.getString("last_activity_type"),
                rs.getObject("last_activity_id", Long.class)
            );
            results.put(forumId, row);
        });
        return results;
    }
}
