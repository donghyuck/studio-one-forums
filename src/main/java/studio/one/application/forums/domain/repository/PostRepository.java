package studio.one.application.forums.domain.repository;

import java.util.List;
import java.util.Optional;
import studio.one.application.forums.domain.model.Post;

/**
 * Forums 도메인 저장소 인터페이스.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public interface PostRepository {
    Post save(Post post);

    Optional<Post> findById(Long postId);

    List<Post> findByTopicId(Long topicId);
}
