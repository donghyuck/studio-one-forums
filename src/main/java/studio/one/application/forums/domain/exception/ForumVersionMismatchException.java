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
public class ForumVersionMismatchException extends PlatformException {
    private static final ErrorType VERSION_MISMATCH = ErrorType.of("error.forums.forum.version.mismatch", HttpStatus.CONFLICT);

    public ForumVersionMismatchException(String forumSlug) {
        super(VERSION_MISMATCH, "Forum Version Mismatch", forumSlug);
    }

    public static ForumVersionMismatchException bySlug(String forumSlug) {
        return new ForumVersionMismatchException(forumSlug);
    }
}
