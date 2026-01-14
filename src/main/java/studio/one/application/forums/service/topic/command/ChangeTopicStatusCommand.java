package studio.one.application.forums.service.topic.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import studio.one.application.forums.domain.type.TopicStatus;

/**
 * Forums 유스케이스 커맨드.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public class ChangeTopicStatusCommand {
    private final String forumSlug;
    private final Long topicId;
    private final TopicStatus status;
    private final Long updatedById;
    private final String updatedBy;
    private final long expectedVersion;
}
