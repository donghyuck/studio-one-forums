package studio.one.application.forums.service.topic.command;

import java.util.List;

public class CreateTopicCommand {
    private final String forumSlug;
    private final Long categoryId;
    private final String title;
    private final List<String> tags;
    private final Long createdById;
    private final String createdBy;

    public CreateTopicCommand(String forumSlug, Long categoryId, String title, List<String> tags, Long createdById, String createdBy) {
        this.forumSlug = forumSlug;
        this.categoryId = categoryId;
        this.title = title;
        this.tags = tags;
        this.createdById = createdById;
        this.createdBy = createdBy;
    }

    public String forumSlug() {
        return forumSlug;
    }

    public Long categoryId() {
        return categoryId;
    }

    public String title() {
        return title;
    }

    public List<String> tags() {
        return tags;
    }

    public Long createdById() {
        return createdById;
    }

    public String createdBy() {
        return createdBy;
    }
}
