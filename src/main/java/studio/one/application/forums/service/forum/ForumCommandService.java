package studio.one.application.forums.service.forum;

import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.exception.ForumSlugConflictException;
import studio.one.application.forums.domain.exception.ForumVersionMismatchException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.service.forum.command.CreateForumCommand;
import studio.one.application.forums.service.forum.command.UpdateForumSettingsCommand;

@Service
public class ForumCommandService {
    private final ForumRepository forumRepository;

    public ForumCommandService(ForumRepository forumRepository) {
        this.forumRepository = forumRepository;
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
            command.createdById(),
            command.createdBy(),
            now,
            command.createdById(),
            command.createdBy(),
            now,
            0L
        );
        return forumRepository.save(forum);
    }

    @Transactional
    public Forum updateSettings(UpdateForumSettingsCommand command) {
        ForumSlug slug = ForumSlug.of(command.slug());
        Forum forum = forumRepository.findBySlug(slug)
            .orElseThrow(() -> ForumNotFoundException.bySlug(slug.value()));
        if (forum.version() != command.expectedVersion()) {
            throw ForumVersionMismatchException.bySlug(slug.value());
        }
        forum.updateSettings(command.name(), command.description(), command.updatedById(), command.updatedBy(), OffsetDateTime.now());
        return forumRepository.save(forum);
    }
}
