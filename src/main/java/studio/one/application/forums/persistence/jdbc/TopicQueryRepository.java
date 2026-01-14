package studio.one.application.forums.persistence.jdbc;

import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Pageable;

/**
 * Forums JDBC 영속성 어댑터.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public interface TopicQueryRepository {
    List<TopicListRow> findTopics(Long forumId, String query, Set<String> inFields, Set<String> fields, Pageable pageable);
}
