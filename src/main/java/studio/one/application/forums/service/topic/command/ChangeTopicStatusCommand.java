package studio.one.application.forums.service.topic.command;

import studio.one.application.forums.domain.type.TopicStatus;

public class ChangeTopicStatusCommand {
    private final String forumSlug;
    private final Long topicId;
    private final TopicStatus status;
    private final Long updatedById;
    private final String updatedBy;
    private final long expectedVersion;

    public ChangeTopicStatusCommand(String forumSlug, Long topicId, TopicStatus status, Long updatedById, String updatedBy, long expectedVersion) {
        this.forumSlug = forumSlug;
        this.topicId = topicId;
        this.status = status;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.expectedVersion = expectedVersion;
    }

    public String forumSlug() {
        return forumSlug;
    }

    public Long topicId() {
        return topicId;
    }

    public TopicStatus status() {
        return status;
    }

    public Long updatedById() {
        return updatedById;
    }

    public String updatedBy() {
        return updatedBy;
    }

    public long expectedVersion() {
        return expectedVersion;
    }
}
