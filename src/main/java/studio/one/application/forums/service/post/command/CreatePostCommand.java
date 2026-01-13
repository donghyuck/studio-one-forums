package studio.one.application.forums.service.post.command;

public class CreatePostCommand {
    private final String forumSlug;
    private final Long topicId;
    private final String content;
    private final Long createdById;
    private final String createdBy;

    public CreatePostCommand(String forumSlug, Long topicId, String content, Long createdById, String createdBy) {
        this.forumSlug = forumSlug;
        this.topicId = topicId;
        this.content = content;
        this.createdById = createdById;
        this.createdBy = createdBy;
    }

    public String forumSlug() {
        return forumSlug;
    }

    public Long topicId() {
        return topicId;
    }

    public String content() {
        return content;
    }

    public Long createdById() {
        return createdById;
    }

    public String createdBy() {
        return createdBy;
    }
}
