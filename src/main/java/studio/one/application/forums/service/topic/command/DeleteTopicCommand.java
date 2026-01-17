package studio.one.application.forums.service.topic.command;

public class DeleteTopicCommand {
    private final String forumSlug;
    private final Long topicId;
    private final Long deletedById;
    private final String deletedBy;
    private final long expectedVersion;

    public DeleteTopicCommand(String forumSlug, Long topicId, Long deletedById, String deletedBy, long expectedVersion) {
        this.forumSlug = forumSlug;
        this.topicId = topicId;
        this.deletedById = deletedById;
        this.deletedBy = deletedBy;
        this.expectedVersion = expectedVersion;
    }

    public String forumSlug() {
        return forumSlug;
    }

    public Long topicId() {
        return topicId;
    }

    public Long deletedById() {
        return deletedById;
    }

    public String deletedBy() {
        return deletedBy;
    }

    public long expectedVersion() {
        return expectedVersion;
    }
}
