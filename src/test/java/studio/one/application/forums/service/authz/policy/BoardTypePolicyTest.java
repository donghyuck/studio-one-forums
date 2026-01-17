package studio.one.application.forums.service.authz.policy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.service.authz.PolicyDecision;

class BoardTypePolicyTest {

    @Test
    void commonAllowsReadAndMemberWrite() {
        BoardTypePolicy policy = new CommonBoardTypePolicy();
        assertThat(policy.decide(PermissionAction.READ_BOARD, false, false, false, false)).isEqualTo(PolicyDecision.ALLOW);
        assertThat(policy.decide(PermissionAction.CREATE_TOPIC, true, false, false, false)).isEqualTo(PolicyDecision.ALLOW);
        assertThat(policy.decide(PermissionAction.CREATE_TOPIC, false, false, false, false)).isEqualTo(PolicyDecision.DENY);
    }

    @Test
    void noticeAllowsReadOnlyForMembers() {
        BoardTypePolicy policy = new NoticeBoardTypePolicy();
        assertThat(policy.decide(PermissionAction.READ_TOPIC_CONTENT, false, false, false, false)).isEqualTo(PolicyDecision.ALLOW);
        assertThat(policy.decide(PermissionAction.REPLY_POST, true, false, false, false)).isEqualTo(PolicyDecision.DENY);
        assertThat(policy.decide(PermissionAction.CREATE_TOPIC, true, true, false, false)).isEqualTo(PolicyDecision.ALLOW);
    }

    @Test
    void secretDeniesContentForNonOwner() {
        BoardTypePolicy policy = new SecretBoardTypePolicy(false);
        assertThat(policy.decide(PermissionAction.READ_TOPIC_CONTENT, true, false, false, false)).isEqualTo(PolicyDecision.DENY);
        assertThat(policy.decide(PermissionAction.READ_TOPIC_CONTENT, true, true, false, false)).isEqualTo(PolicyDecision.ALLOW);
        assertThat(policy.decide(PermissionAction.READ_TOPIC_CONTENT, true, false, true, false)).isEqualTo(PolicyDecision.ALLOW);
    }

    @Test
    void adminOnlyDeniesNonAdmin() {
        BoardTypePolicy policy = new AdminOnlyBoardTypePolicy();
        assertThat(policy.decide(PermissionAction.READ_BOARD, true, false, false, false)).isEqualTo(PolicyDecision.DENY);
        assertThat(policy.decide(PermissionAction.READ_BOARD, true, true, false, false)).isEqualTo(PolicyDecision.ALLOW);
    }

    @Test
    void lockedTopicBlocksReplyForMember() {
        BoardTypePolicy policy = new CommonBoardTypePolicy();
        assertThat(policy.decide(PermissionAction.REPLY_POST, true, false, false, true)).isEqualTo(PolicyDecision.DENY);
        assertThat(policy.decide(PermissionAction.REPLY_POST, true, true, false, true)).isEqualTo(PolicyDecision.ALLOW);
    }
}
