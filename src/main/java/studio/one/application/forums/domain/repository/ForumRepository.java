package studio.one.application.forums.domain.repository;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Forum> search(String query, Set<String> inFields, Pageable pageable);

    java.util.List<Forum> searchCandidates(String query, Set<String> inFields, boolean isAdmin,
                                           boolean isMember, boolean secretListVisible, Long userId);

    Page<Forum> searchCandidatesPage(String query, Set<String> inFields, boolean isAdmin,
                                     boolean isMember, boolean secretListVisible, Long userId, Pageable pageable);
}
