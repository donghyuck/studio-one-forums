package studio.one.application.forums.service.topic.command;

import java.util.List;

public class UpdateTopicCommand {
    private final String forumSlug;
    private final Long topicId;
    private final String title;
    private final List<String> tags;
    private final Long updatedById;
    private final String updatedBy;
    private final long expectedVersion;

    public UpdateTopicCommand(String forumSlug, Long topicId, String title, List<String> tags,
                              Long updatedById, String updatedBy, long expectedVersion) {
        this.forumSlug = forumSlug;
        this.topicId = topicId;
        this.title = title;
        this.tags = tags;
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

    public String title() {
        return title;
    }

    public List<String> tags() {
        return tags;
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
