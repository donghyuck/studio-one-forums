package studio.one.application.forums.domain.acl;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public class ForumAclRule {
    private final Long ruleId;
    private final Long forumId;
    private final Long categoryId;
    private final SubjectType subjectType;
    private final IdentifierType identifierType;
    private final Long subjectId;
    private final String subjectName;
    private final String role;
    private final PermissionAction action;
    private final Effect effect;
    private final Ownership ownership;
    private final int priority;
    private final boolean enabled;
    private final Long createdById;
    private final OffsetDateTime createdAt;
    private final Long updatedById;
    private final OffsetDateTime updatedAt;
}
