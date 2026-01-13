package studio.one.application.forums.service.forum.command;

public class CreateForumCommand {
    private final String slug;
    private final String name;
    private final String description;
    private final Long createdById;
    private final String createdBy;

    public CreateForumCommand(String slug, String name, String description, Long createdById, String createdBy) {
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.createdById = createdById;
        this.createdBy = createdBy;
    }

    public String slug() {
        return slug;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Long createdById() {
        return createdById;
    }

    public String createdBy() {
        return createdBy;
    }
}
