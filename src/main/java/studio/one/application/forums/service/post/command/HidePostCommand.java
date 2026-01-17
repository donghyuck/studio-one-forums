package studio.one.application.forums.service.post.command;

public class HidePostCommand {
    private final Long postId;
    private final boolean hidden;
    private final String reason;
    private final Long updatedById;
    private final String updatedBy;
    private final long expectedVersion;

    public HidePostCommand(Long postId, boolean hidden, String reason, Long updatedById, String updatedBy, long expectedVersion) {
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
