package studio.one.application.forums.domain.exception;

import studio.one.platform.error.ErrorType;
import studio.one.platform.exception.NotFoundException;
import org.springframework.http.HttpStatus;

public class TopicNotFoundException extends NotFoundException {
    private static final ErrorType BY_ID = ErrorType.of("error.forums.topic.not.found.id", HttpStatus.NOT_FOUND);
    private static final ErrorType BY_FORUM = ErrorType.of("error.forums.topic.not.found.in.forum", HttpStatus.NOT_FOUND);

    public TopicNotFoundException(ErrorType errorType, Object target) {
        super(errorType, "Topic Not Found", target);
    }

    public static TopicNotFoundException byId(Long topicId) {
        return new TopicNotFoundException(BY_ID, topicId);
    }

    public static TopicNotFoundException inForum(Long topicId, Long forumId) {
        return new TopicNotFoundException(BY_FORUM, "topicId=" + topicId + ", forumId=" + forumId);
    }
}
