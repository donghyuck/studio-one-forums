package studio.one.application.forums.domain.policy;

import studio.one.application.forums.domain.type.TopicStatus;

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
