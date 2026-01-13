package studio.one.application.forums.domain.exception;

import org.springframework.http.HttpStatus;

import studio.one.platform.error.ErrorType;
import studio.one.platform.exception.PlatformException;

public class TopicStatusTransitionNotAllowedException extends PlatformException {
    private static final ErrorType NOT_ALLOWED = ErrorType.of("error.forums.topic.status.transition.not.allowed", HttpStatus.BAD_REQUEST);

    public TopicStatusTransitionNotAllowedException(String fromStatus, String toStatus) {
        super(NOT_ALLOWED, "Topic Status Transition Not Allowed", "from=" + fromStatus + ", to=" + toStatus);
    }

    public static TopicStatusTransitionNotAllowedException of(String fromStatus, String toStatus) {
        return new TopicStatusTransitionNotAllowedException(fromStatus, toStatus);
    }
}
