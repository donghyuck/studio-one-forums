package studio.one.application.forums.service.post.command;

public class HidePostCommand {
    private final String forumSlug;
    private final Long topicId;
    private final Long postId;
    private final boolean hidden;
    private final String reason;
    private final Long updatedById;
    private final String updatedBy;
    private final long expectedVersion;

    public HidePostCommand(String forumSlug, Long topicId, Long postId, boolean hidden, String reason,
                           Long updatedById, String updatedBy, long expectedVersion) {
        this.forumSlug = forumSlug;
        this.topicId = topicId;
        this.postId = postId;
        this.hidden = hidden;
        this.reason = reason;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.expectedVersion = expectedVersion;
    }

    public Long postId() {
        return postId;
    }

    public String forumSlug() {
        return forumSlug;
    }

    public Long topicId() {
        return topicId;
    }

    public boolean hidden() {
        return hidden;
    }

    public String reason() {
        return reason;
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
