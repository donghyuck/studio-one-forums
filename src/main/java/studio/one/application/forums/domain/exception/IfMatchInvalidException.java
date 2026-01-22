package studio.one.application.forums.domain.exception;

import org.springframework.http.HttpStatus;
import studio.one.platform.error.ErrorType;
import studio.one.platform.exception.PlatformException;

public class IfMatchInvalidException extends PlatformException {
    private static final ErrorType INVALID = ErrorType.of(
        "error.http.precondition.failed",
        HttpStatus.PRECONDITION_FAILED
    );

    public IfMatchInvalidException(String value) {
        super(INVALID, "Invalid If-Match header", value);
    }
}
