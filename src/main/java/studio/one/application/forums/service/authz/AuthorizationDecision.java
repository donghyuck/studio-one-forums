package studio.one.application.forums.service.authz;

public class AuthorizationDecision {
    private final boolean allowed;
    private final PolicyDecision policyDecision;
    private final PolicyDecision aclDecision;
    private final DenyReason denyReason;

    private AuthorizationDecision(boolean allowed, PolicyDecision policyDecision, PolicyDecision aclDecision, DenyReason denyReason) {
        this.allowed = allowed;
        this.policyDecision = policyDecision;
        this.aclDecision = aclDecision;
        this.denyReason = denyReason;
    }

    public static AuthorizationDecision allow(PolicyDecision policyDecision, PolicyDecision aclDecision) {
        return new AuthorizationDecision(true, policyDecision, aclDecision, null);
    }

    public static AuthorizationDecision deny(PolicyDecision policyDecision, PolicyDecision aclDecision, DenyReason denyReason) {
        return new AuthorizationDecision(false, policyDecision, aclDecision, denyReason);
    }

    public boolean isAllowed() {
        return allowed;
    }

    public PolicyDecision getPolicyDecision() {
        return policyDecision;
    }

    public PolicyDecision getAclDecision() {
        return aclDecision;
    }

    public DenyReason getDenyReason() {
        return denyReason;
    }
}
