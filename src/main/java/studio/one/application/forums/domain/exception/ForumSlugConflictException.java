package studio.one.application.forums.domain.exception;

import org.springframework.http.HttpStatus;

import studio.one.platform.error.ErrorType;
import studio.one.platform.exception.PlatformException;

public class ForumSlugConflictException extends PlatformException {
    private static final ErrorType BY_SLUG = ErrorType.of("error.forums.forum.slug.conflict", HttpStatus.CONFLICT);

    public ForumSlugConflictException(String forumSlug) {
        super(BY_SLUG, "Forum Slug Conflict", forumSlug);
    }

    public static ForumSlugConflictException bySlug(String forumSlug) {
        return new ForumSlugConflictException(forumSlug);
    }
}
