package studio.one.application.forums.service.topic.command;

public class LockTopicCommand {
    private final String forumSlug;
    private final Long topicId;
    private final boolean locked;
    private final Long updatedById;
    private final String updatedBy;
    private final long expectedVersion;

    public LockTopicCommand(String forumSlug, Long topicId, boolean locked,
                            Long updatedById, String updatedBy, long expectedVersion) {
        this.forumSlug = forumSlug;
        this.topicId = topicId;
        this.locked = locked;
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

    public boolean locked() {
        return locked;
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
