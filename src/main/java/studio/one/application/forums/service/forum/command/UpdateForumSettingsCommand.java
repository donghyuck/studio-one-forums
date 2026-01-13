package studio.one.application.forums.service.forum.command;

public class UpdateForumSettingsCommand {
    private final String slug;
    private final String name;
    private final String description;
    private final Long updatedById;
    private final String updatedBy;
    private final long expectedVersion;

    public UpdateForumSettingsCommand(String slug, String name, String description, Long updatedById, String updatedBy, long expectedVersion) {
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.expectedVersion = expectedVersion;
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
