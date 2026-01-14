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
public class CategoryNotFoundException extends NotFoundException {
    
    private static final ErrorType BY_ID = ErrorType.of("error.forums.category.not.found.id", HttpStatus.NOT_FOUND);

    public CategoryNotFoundException(Long categoryId) {
        super(BY_ID, "Category Not Found", categoryId);
    }

    public static CategoryNotFoundException byId(Long categoryId) {
        return new CategoryNotFoundException(categoryId);
    }
}
