package studio.one.application.forums.persistence.jpa;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import studio.one.application.forums.domain.model.ForumMember;
import studio.one.application.forums.domain.repository.ForumMemberRepository;
import studio.one.application.forums.domain.type.ForumMemberRole;
import studio.one.application.forums.persistence.jpa.entity.ForumMemberEntity;
import studio.one.application.forums.persistence.jpa.entity.ForumMemberId;
import studio.one.application.forums.persistence.jpa.repo.ForumMemberJpaRepository;

@Repository
public class ForumMemberRepositoryAdapter implements ForumMemberRepository {
    private final ForumMemberJpaRepository forumMemberJpaRepository;

    public ForumMemberRepositoryAdapter(ForumMemberJpaRepository forumMemberJpaRepository) {
        this.forumMemberJpaRepository = forumMemberJpaRepository;
    }

    @Override
    public Optional<ForumMemberRole> findRole(long forumId, long userId) {
        return forumMemberJpaRepository.findByIdForumIdAndIdUserId(forumId, userId)
            .map(ForumMemberEntity::getRole);
    }

    @Override
    public Set<ForumMemberRole> findRoles(long forumId, long userId) {
        return findRole(forumId, userId).map(Set::of).orElseGet(Set::of);
    }

    @Override
    public java.util.Map<Long, Set<ForumMemberRole>> findRolesByUserId(long userId) {
        List<ForumMemberEntity> members = forumMemberJpaRepository.findByIdUserId(userId);
        if (members.isEmpty()) {
            return java.util.Map.of();
        }
        java.util.Map<Long, Set<ForumMemberRole>> result = new java.util.HashMap<>();
        for (ForumMemberEntity member : members) {
            Long forumId = member.getId().getForumId();
            if (forumId == null) {
                continue;
            }
            result.computeIfAbsent(forumId, key -> new java.util.HashSet<>()).add(member.getRole());
        }
        return result;
    }

    @Override
    public List<ForumMember> listMembers(long forumId, int page, int size) {
        return forumMemberJpaRepository.findByIdForumId(forumId, PageRequest.of(page, size))
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void upsertMemberRole(long forumId, long userId, ForumMemberRole role, Long actorId) {
        ForumMemberId id = new ForumMemberId(forumId, userId);
        ForumMemberEntity entity = forumMemberJpaRepository.findById(id)
            .orElseGet(() -> new ForumMemberEntity(id, role, actorId, OffsetDateTime.now()));
        entity.setRole(role);
        forumMemberJpaRepository.save(entity);
    }

    @Override
    public void removeMember(long forumId, long userId) {
        forumMemberJpaRepository.deleteById(new ForumMemberId(forumId, userId));
    }

    private ForumMember toDomain(ForumMemberEntity entity) {
        return new ForumMember(
            entity.getId().getForumId(),
            entity.getId().getUserId(),
            entity.getRole(),
            entity.getCreatedById(),
            entity.getCreatedAt()
        );
    }
}
