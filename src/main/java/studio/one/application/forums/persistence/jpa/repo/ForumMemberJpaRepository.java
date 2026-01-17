package studio.one.application.forums.persistence.jpa.repo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import studio.one.application.forums.persistence.jpa.entity.ForumMemberEntity;
import studio.one.application.forums.persistence.jpa.entity.ForumMemberId;

public interface ForumMemberJpaRepository extends JpaRepository<ForumMemberEntity, ForumMemberId> {
    Optional<ForumMemberEntity> findByIdForumIdAndIdUserId(Long forumId, Long userId);

    List<ForumMemberEntity> findByIdForumId(Long forumId, Pageable pageable);

    List<ForumMemberEntity> findByIdUserId(Long userId);
}
