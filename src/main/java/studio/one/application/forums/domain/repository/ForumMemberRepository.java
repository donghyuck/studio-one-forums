package studio.one.application.forums.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import studio.one.application.forums.domain.model.ForumMember;
import studio.one.application.forums.domain.type.ForumMemberRole;

public interface ForumMemberRepository {
    Optional<ForumMemberRole> findRole(long forumId, long userId);

    Set<ForumMemberRole> findRoles(long forumId, long userId);

    java.util.Map<Long, Set<ForumMemberRole>> findRolesByUserId(long userId);

    List<ForumMember> listMembers(long forumId, int page, int size);

    void upsertMemberRole(long forumId, long userId, ForumMemberRole role, Long actorId);

    void removeMember(long forumId, long userId);
}
