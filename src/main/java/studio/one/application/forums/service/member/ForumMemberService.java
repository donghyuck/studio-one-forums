package studio.one.application.forums.service.member;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.model.ForumMember;
import studio.one.application.forums.domain.repository.ForumMemberRepository;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.type.ForumMemberRole;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.service.audit.ForumAuditLogService;

@Service
public class ForumMemberService {
    private final ForumRepository forumRepository;
    private final ForumMemberRepository forumMemberRepository;
    private final ForumAuditLogService auditLogService;

    public ForumMemberService(ForumRepository forumRepository,
                              ForumMemberRepository forumMemberRepository,
                              ForumAuditLogService auditLogService) {
        this.forumRepository = forumRepository;
        this.forumMemberRepository = forumMemberRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public void addOrUpdateMemberRole(String forumSlug, long userId, ForumMemberRole role, Long actorId) {
        Forum forum = forumRepository.findBySlug(ForumSlug.of(forumSlug))
            .orElseThrow(() -> ForumNotFoundException.bySlug(forumSlug));
        forumMemberRepository.upsertMemberRole(forum.id(), userId, role, actorId);
        auditLogService.record(forum.id(), "FORUM_MEMBER", userId, "MEMBER_UPSERT", actorId,
            java.util.Map.of("role", role.name()));
    }

    @Transactional
    public void addOrUpdateMemberRole(Long forumId, long userId, ForumMemberRole role, Long actorId) {
        forumMemberRepository.upsertMemberRole(forumId, userId, role, actorId);
        auditLogService.record(forumId, "FORUM_MEMBER", userId, "MEMBER_UPSERT", actorId,
            java.util.Map.of("role", role.name()));
    }

    @Transactional
    public void removeMember(String forumSlug, long userId, Long actorId) {
        Forum forum = forumRepository.findBySlug(ForumSlug.of(forumSlug))
            .orElseThrow(() -> ForumNotFoundException.bySlug(forumSlug));
        forumMemberRepository.removeMember(forum.id(), userId);
        auditLogService.record(forum.id(), "FORUM_MEMBER", userId, "MEMBER_REMOVE", actorId, null);
    }

    @Transactional
    public void removeMember(long forumId, long userId, Long actorId) {
        forumMemberRepository.removeMember(forumId, userId);
        auditLogService.record(forumId, "FORUM_MEMBER", userId, "MEMBER_REMOVE", actorId, null);
    }

    public List<ForumMember> listMembers(String forumSlug, int page, int size) {
        Forum forum = forumRepository.findBySlug(ForumSlug.of(forumSlug))
            .orElseThrow(() -> ForumNotFoundException.bySlug(forumSlug));
        return forumMemberRepository.listMembers(forum.id(), page, size);
    }
}
