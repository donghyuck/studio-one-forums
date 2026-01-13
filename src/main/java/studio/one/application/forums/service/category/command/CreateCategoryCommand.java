package studio.one.application.forums.service.category.command;

public class CreateCategoryCommand {
    private final String forumSlug;
    private final String name;
    private final String description;
    private final int position;
    private final Long createdById;
    private final String createdBy;

    public CreateCategoryCommand(String forumSlug, String name, String description, int position, Long createdById, String createdBy) {
        this.forumSlug = forumSlug;
        this.name = name;
        this.description = description;
        this.position = position;
        this.createdById = createdById;
        this.createdBy = createdBy;
    }

    public String forumSlug() {
        return forumSlug;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public int position() {
        return position;
    }

    public Long createdById() {
        return createdById;
    }

    public String createdBy() {
        return createdBy;
    }
}
