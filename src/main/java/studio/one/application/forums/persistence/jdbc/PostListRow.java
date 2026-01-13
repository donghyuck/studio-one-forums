package studio.one.application.forums.persistence.jdbc;

import java.time.OffsetDateTime;

public class PostListRow {
    
    private final Long postId;
    private final String content;
    private final Long createdById;
    private final String createdBy;
    private final OffsetDateTime createdAt;

    public PostListRow(Long postId, String content, Long createdById, String createdBy, OffsetDateTime createdAt) {
        this.postId = postId;
        this.content = content;
        this.createdById = createdById;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public Long getPostId() {
        return postId;
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
