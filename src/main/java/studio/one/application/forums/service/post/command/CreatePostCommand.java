package studio.one.application.forums.service.post.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

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
public class CreatePostCommand {
    private final String forumSlug;
    private final Long topicId;
    private final String content;
    private final Long createdById;
    private final String createdBy;
}
