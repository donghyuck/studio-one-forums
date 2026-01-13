package studio.one.application.forums.domain.event;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public class TopicCreatedEvent {
    private final Long topicId;
    private final OffsetDateTime occurredAt;
}
