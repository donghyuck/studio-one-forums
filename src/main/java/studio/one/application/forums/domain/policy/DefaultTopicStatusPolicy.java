package studio.one.application.forums.domain.policy;

import studio.one.application.forums.domain.type.TopicStatus;

/**
 * Forums 도메인 정책.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public class DefaultTopicStatusPolicy implements TopicStatusPolicy {
    @Override
    public boolean canTransition(TopicStatus from, TopicStatus to) {
        if (from == to) {
            return true;
        }
        if (from == TopicStatus.ARCHIVED) {
            return false;
        }
        return true;
    }
}
