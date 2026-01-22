package studio.one.application.forums.domain.exception;

import org.springframework.http.HttpStatus;
import studio.one.platform.error.ErrorType;
import studio.one.platform.exception.PlatformException;

public class IfMatchRequiredException extends PlatformException {
    private static final ErrorType REQUIRED = ErrorType.of(
        "error.http.precondition.required",
        HttpStatus.PRECONDITION_REQUIRED
    );

    public IfMatchRequiredException() {
        super(REQUIRED, "If-Match header is required", "If-Match");
    }
}
