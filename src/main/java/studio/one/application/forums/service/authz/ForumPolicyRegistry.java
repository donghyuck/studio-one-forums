package studio.one.application.forums.service.authz;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.service.authz.policy.ForumTypePolicy;

@Component
public class ForumPolicyRegistry {
    private final Map<ForumType, ForumTypePolicy> policies;

    public ForumPolicyRegistry(List<ForumTypePolicy> policyList) {
        this.policies = new EnumMap<>(ForumType.class);
        for (ForumTypePolicy policy : policyList) {
            this.policies.put(policy.type(), policy);
        }
        for (ForumType type : ForumType.values()) {
            if (!policies.containsKey(type)) {
                throw new IllegalStateException("Missing ForumTypePolicy for " + type);
            }
        }
    }

    public ForumTypePolicy get(ForumType type) {
        ForumType resolved = type != null ? type : ForumType.COMMON;
        ForumTypePolicy policy = policies.get(resolved);
        if (policy == null) {
            throw new IllegalStateException("Missing ForumTypePolicy for " + resolved);
        }
        return policy;
    }
}
