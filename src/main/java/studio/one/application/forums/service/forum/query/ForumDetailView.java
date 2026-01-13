package studio.one.application.forums.service.forum.query;

import java.time.OffsetDateTime;

public class ForumDetailView {
    private final Long id;
    private final String slug;
    private final String name;
    private final String description;
    private final OffsetDateTime updatedAt;
    private final long version;

    public ForumDetailView(Long id, String slug, String name, String description, OffsetDateTime updatedAt, long version) {
        this.id = id;
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}
