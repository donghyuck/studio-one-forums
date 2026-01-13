package studio.one.application.forums.domain.exception;

import org.springframework.http.HttpStatus;

import studio.one.platform.error.ErrorType;
import studio.one.platform.exception.PlatformException;

public class TopicVersionMismatchException extends PlatformException {
    private static final ErrorType VERSION_MISMATCH = ErrorType.of("error.forums.topic.version.mismatch", HttpStatus.CONFLICT);

    public TopicVersionMismatchException(Long topicId) {
        super(VERSION_MISMATCH, "Topic Version Mismatch", topicId);
    }

    public static TopicVersionMismatchException byId(Long topicId) {
        return new TopicVersionMismatchException(topicId);
    }
}
