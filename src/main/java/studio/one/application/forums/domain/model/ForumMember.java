package studio.one.application.forums.domain.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import studio.one.application.forums.domain.type.ForumMemberRole;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public class ForumMember {
    private final Long forumId;
    private final Long userId;
    private final ForumMemberRole role;
    private final Long createdById;
    private final OffsetDateTime createdAt;
}
