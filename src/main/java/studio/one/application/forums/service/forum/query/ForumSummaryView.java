package studio.one.application.forums.service.forum.query;

import java.time.OffsetDateTime;

public class ForumSummaryView {
    private final String slug;
    private final String name;
    private final OffsetDateTime updatedAt;

    public ForumSummaryView(String slug, String name, OffsetDateTime updatedAt) {
        this.slug = slug;
        this.name = name;
        this.updatedAt = updatedAt;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
