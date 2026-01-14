package studio.one.application.forums.persistence.jdbc;

import java.util.List;
import org.springframework.data.domain.Pageable;

/**
 * Forums JDBC 영속성 어댑터.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public interface PostQueryRepository {
    List<PostListRow> findPosts(Long topicId, Pageable pageable);
}
