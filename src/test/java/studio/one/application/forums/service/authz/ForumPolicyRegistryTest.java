package studio.one.application.forums.service.authz;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import studio.one.application.forums.service.authz.policy.CommonBoardTypePolicy;
import studio.one.application.forums.service.authz.policy.SecretBoardTypePolicy;

class ForumPolicyRegistryTest {
    @Test
    void throwsWhenPolicyMissing() {
        assertThatThrownBy(() -> new ForumPolicyRegistry(List.of(
            new CommonBoardTypePolicy(),
            new SecretBoardTypePolicy(false)
        ))).isInstanceOf(IllegalStateException.class);
    }
}
