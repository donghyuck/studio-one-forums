package studio.one.application.forums.service.post.command;

public class DeletePostCommand {
    private final Long postId;
    private final Long deletedById;
    private final String deletedBy;
    private final long expectedVersion;

    public DeletePostCommand(Long postId, Long deletedById, String deletedBy, long expectedVersion) {
        this.postId = postId;
        this.deletedById = deletedById;
        this.deletedBy = deletedBy;
        this.expectedVersion = expectedVersion;
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
