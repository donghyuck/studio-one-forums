package studio.one.application.forums.domain.event;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Forums 도메인 이벤트.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public class PostCreatedEvent implements ForumsCacheEvictableEvent {
    private final String forumSlug;
    private final Long topicId;
    private final OffsetDateTime occurredAt;
}
