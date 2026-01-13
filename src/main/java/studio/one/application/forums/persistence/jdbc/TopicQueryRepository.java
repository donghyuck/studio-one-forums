package studio.one.application.forums.persistence.jdbc;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;

public interface TopicQueryRepository {
    List<TopicListRow> findTopics(Long forumId, String query, Set<String> inFields, Set<String> fields, Pageable pageable);
}
