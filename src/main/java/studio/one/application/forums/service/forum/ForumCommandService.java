package studio.one.application.forums.service.forum;

import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.one.application.forums.domain.event.ForumCreatedEvent;
import studio.one.application.forums.domain.event.ForumUpdatedEvent;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.exception.ForumSlugConflictException;
import studio.one.application.forums.domain.exception.ForumVersionMismatchException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.service.member.ForumMemberService;
import studio.one.application.forums.service.forum.command.CreateForumCommand;
import studio.one.application.forums.service.forum.command.UpdateForumSettingsCommand;

/**
 * Forums 명령 서비스.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Service
public class ForumCommandService {
    private final ForumRepository forumRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ForumMemberService forumMemberService;

    public ForumCommandService(ForumRepository forumRepository,
                               ApplicationEventPublisher eventPublisher,
                               ForumMemberService forumMemberService) {
        this.forumRepository = forumRepository;
        this.eventPublisher = eventPublisher;
        this.forumMemberService = forumMemberService;
    }

    @Transactional
    public Forum createForum(CreateForumCommand command) {
        ForumSlug slug = ForumSlug.of(command.slug());
        if (forumRepository.existsBySlug(slug)) {
            throw ForumSlugConflictException.bySlug(slug.value());
        }
        OffsetDateTime now = OffsetDateTime.now();
        Forum forum = new Forum(
            null,
            slug,
            command.name(),
            command.description(),
            ForumType.COMMON,
            Map.of(),
            command.createdById(),
            command.createdBy(),
            now,
            command.createdById(),
            command.createdBy(),
            now,
            0L
        );
        Forum saved = forumRepository.save(forum);
        forumMemberService.addOrUpdateMemberRole(saved.id(), command.createdById(), studio.one.application.forums.domain.type.ForumMemberRole.OWNER, command.createdById());
        eventPublisher.publishEvent(new ForumCreatedEvent(saved.slug().value(), null, now));
        return saved;
    }

    @Transactional
    public Forum updateSettings(UpdateForumSettingsCommand command) {
        ForumSlug slug = ForumSlug.of(command.slug());
        Forum forum = forumRepository.findBySlug(slug)
            .orElseThrow(() -> ForumNotFoundException.bySlug(slug.value()));
        if (forum.version() != command.expectedVersion()) {
            throw ForumVersionMismatchException.bySlug(slug.value());
        }
        OffsetDateTime now = OffsetDateTime.now();
        forum.updateSettings(command.name(), command.description(), command.updatedById(), command.updatedBy(), now);
        Forum saved = forumRepository.save(forum);
        eventPublisher.publishEvent(new ForumUpdatedEvent(saved.slug().value(), null, now));
        return saved;
    }
}
