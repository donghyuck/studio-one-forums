package studio.one.application.forums.domain.repository;

import java.util.Optional;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.vo.ForumSlug;

public interface ForumRepository {
    Forum save(Forum forum);

    Optional<Forum> findById(Long forumId);

    Optional<Forum> findBySlug(ForumSlug slug);

    boolean existsBySlug(ForumSlug slug);

    java.util.List<Forum> findAll();
}
