package studio.one.application.forums.domain.exception;

import org.springframework.http.HttpStatus;

import studio.one.platform.error.ErrorType;
import studio.one.platform.exception.PlatformException;

public class CategoryForumMismatchException extends PlatformException {
    private static final ErrorType MISMATCH = ErrorType.of("error.forums.category.mismatch.forum", HttpStatus.BAD_REQUEST);

    public CategoryForumMismatchException(Long categoryId, Long forumId) {
        super(MISMATCH, "Category Forum Mismatch", "categoryId=" + categoryId + ", forumId=" + forumId);
    }

    public static CategoryForumMismatchException of(Long categoryId, Long forumId) {
        return new CategoryForumMismatchException(categoryId, forumId);
    }
}
