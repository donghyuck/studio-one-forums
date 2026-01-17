package studio.one.application.forums.service.topic.command;

public class PinTopicCommand {
    private final String forumSlug;
    private final Long topicId;
    private final boolean pinned;
    private final Long updatedById;
    private final String updatedBy;
    private final long expectedVersion;

    public PinTopicCommand(String forumSlug, Long topicId, boolean pinned,
                           Long updatedById, String updatedBy, long expectedVersion) {
        this.forumSlug = forumSlug;
        this.topicId = topicId;
        this.pinned = pinned;
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

    public boolean pinned() {
        return pinned;
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
