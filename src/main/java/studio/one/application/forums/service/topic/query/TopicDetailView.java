package studio.one.application.forums.service.topic.query;

import java.time.OffsetDateTime;
import java.util.List;

public class TopicDetailView {
    private final Long id;
    private final Long categoryId;
    private final String title;
    private final List<String> tags;
    private final String status;
    private final OffsetDateTime updatedAt;
    private final long version;

    public TopicDetailView(Long id, Long categoryId, String title, List<String> tags, String status,
                           OffsetDateTime updatedAt, long version) {
        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.tags = tags;
        this.status = status;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}
