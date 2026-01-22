package studio.one.application.forums.persistence.jdbc;

import java.util.List;
import java.util.Map;

/**
 * Forums 목록 요약 조회.
 */
public interface ForumQueryRepository {
    Map<Long, ForumSummaryMetricsRow> findForumSummaries(List<Long> forumIds, boolean includeHiddenPosts);
}
