package studio.one.application.forums.service.post.command;

public class UpdatePostCommand {
    private final Long postId;
    private final String content;
    private final Long updatedById;
    private final String updatedBy;
    private final long expectedVersion;

    public UpdatePostCommand(Long postId, String content, Long updatedById, String updatedBy, long expectedVersion) {
        this.postId = postId;
        this.content = content;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.expectedVersion = expectedVersion;
    }

    public Long postId() {
        return postId;
    }

    public String content() {
        return content;
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
