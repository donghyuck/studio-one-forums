package studio.one.application.forums.domain.repository;

import java.util.Optional;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.vo.ForumSlug;

/**
 * Forums 도메인 저장소 인터페이스.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public interface ForumRepository {
    Forum save(Forum forum);

    Optional<Forum> findById(Long forumId);

    Optional<Forum> findBySlug(ForumSlug slug);

    boolean existsBySlug(ForumSlug slug);

    java.util.List<Forum> findAll();
}
