package studio.one.application.forums.domain.exception;

import org.springframework.http.HttpStatus;

import studio.one.platform.error.ErrorType;
import studio.one.platform.exception.NotFoundException;

/**
 * Forums 도메인 예외.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public class ForumNotFoundException extends NotFoundException {
    
    private static final ErrorType BY_SLUG = ErrorType.of("error.forums.forum.not.found.slug", HttpStatus.NOT_FOUND);

    public ForumNotFoundException(String forumSlug) {
        super(BY_SLUG, "Forum Not Found", forumSlug);
    }

    public static ForumNotFoundException bySlug(String forumSlug) {
        return new ForumNotFoundException(forumSlug);
    }
}
