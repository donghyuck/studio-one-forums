package studio.one.application.forums.service.post.query;

import java.time.OffsetDateTime;

public class PostSummaryView {
    private final Long id;
    private final String content;
    private final Long createdById;
    private final String createdBy;
    private final OffsetDateTime createdAt;

    public PostSummaryView(Long id, String content, Long createdById, String createdBy, OffsetDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.createdById = createdById;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
