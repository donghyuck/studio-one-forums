package studio.one.application.forums.service.post.command;

public class DeletePostCommand {
    private final String forumSlug;
    private final Long topicId;
    private final Long postId;
    private final Long deletedById;
    private final String deletedBy;
    private final long expectedVersion;

    public DeletePostCommand(String forumSlug, Long topicId, Long postId, Long deletedById, String deletedBy,
                             long expectedVersion) {
        this.forumSlug = forumSlug;
        this.topicId = topicId;
        this.postId = postId;
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

    public Long postId() {
        return postId;
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
