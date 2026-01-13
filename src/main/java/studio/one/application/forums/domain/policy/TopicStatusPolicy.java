package studio.one.application.forums.domain.policy;

import studio.one.application.forums.domain.type.TopicStatus;

public interface TopicStatusPolicy {
    boolean canTransition(TopicStatus from, TopicStatus to);
}
