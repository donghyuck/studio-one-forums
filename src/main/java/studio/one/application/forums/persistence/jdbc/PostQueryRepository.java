package studio.one.application.forums.persistence.jdbc;

import java.util.List;
import org.springframework.data.domain.Pageable;

public interface PostQueryRepository {
    List<PostListRow> findPosts(Long topicId, Pageable pageable);
}
