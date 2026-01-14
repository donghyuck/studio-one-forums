package studio.one.application.forums.domain.exception;

import org.springframework.http.HttpStatus;

import studio.one.platform.error.ErrorType;
import studio.one.platform.exception.PlatformException;

/**
 * Forums 도메인 예외.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public class ForumSlugConflictException extends PlatformException {
    private static final ErrorType BY_SLUG = ErrorType.of("error.forums.forum.slug.conflict", HttpStatus.CONFLICT);

    public ForumSlugConflictException(String forumSlug) {
        super(BY_SLUG, "Forum Slug Conflict", forumSlug);
    }

    public static ForumSlugConflictException bySlug(String forumSlug) {
        return new ForumSlugConflictException(forumSlug);
    }
}
