package studio.one.application.forums.web.dto;

import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.Setter;
import studio.one.application.forums.domain.acl.Effect;
import studio.one.application.forums.domain.acl.IdentifierType;
import studio.one.application.forums.domain.acl.Ownership;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.acl.SubjectType;
import studio.one.application.forums.service.authz.DenyReason;
import studio.one.application.forums.service.authz.PolicyDecision;

public class ForumPermissionDtos {

    @Getter
    @Setter
    public static class RuleRequest {
        private Long categoryId;
        private String role;
        private SubjectType subjectType;
        private IdentifierType identifierType;
        private Long subjectId;
        private String subjectName;
        private PermissionAction action;
        private Effect effect;
        private Ownership ownership;
        private int priority = 0;
        private boolean enabled = true;
    }

    @Getter
    @Setter
    public static class RuleResponse {
        private Long ruleId;
        private Long categoryId;
        private String role;
        private SubjectType subjectType;
        private IdentifierType identifierType;
        private Long subjectId;
        private String subjectName;
        private PermissionAction action;
        private Effect effect;
        private Ownership ownership;
        private int priority;
        private boolean enabled;
        private Long createdById;
        private OffsetDateTime createdAt;
        private Long updatedById;
        private OffsetDateTime updatedAt;
    }

    @Getter
    @Setter
    public static class SimulationResponse {
        private String action;
        private String role;
        private Long categoryId;
        private boolean allowed;
        private PolicyDecision policyDecision;
        private PolicyDecision aclDecision;
        private DenyReason denyReason;
        private String message;
    }
}
