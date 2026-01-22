package studio.one.application.forums.persistence.jdbc;

import java.time.OffsetDateTime;

/**
 * Forums JDBC 영속성 어댑터.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public class TopicListRow {
    private final Long topicId;
    private final String title;
    private final String status;
    private final OffsetDateTime updatedAt;
    private final Long createdById;
    private final String createdBy;
    private final long postCount;
    private final OffsetDateTime lastPostUpdatedAt;
    private final Long lastPostUpdatedById;
    private final String lastPostUpdatedBy;
    private final Long lastPostId;
    private final OffsetDateTime lastActivityAt;
    private final String excerpt;

    public TopicListRow(Long topicId, String title, String status, OffsetDateTime updatedAt,
                        Long createdById, String createdBy, long postCount,
                        OffsetDateTime lastPostUpdatedAt, Long lastPostUpdatedById, String lastPostUpdatedBy,
                        Long lastPostId, OffsetDateTime lastActivityAt, String excerpt) {
        this.topicId = topicId;
        this.title = title;
        this.status = status;
        this.updatedAt = updatedAt;
        this.createdById = createdById;
        this.createdBy = createdBy;
        this.postCount = postCount;
        this.lastPostUpdatedAt = lastPostUpdatedAt;
        this.lastPostUpdatedById = lastPostUpdatedById;
        this.lastPostUpdatedBy = lastPostUpdatedBy;
        this.lastPostId = lastPostId;
        this.lastActivityAt = lastActivityAt;
        this.excerpt = excerpt;
    }

    public Long getTopicId() {
        return topicId;
    }

    public String getTitle() {
        return title;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public long getPostCount() {
        return postCount;
    }

    public OffsetDateTime getLastPostUpdatedAt() {
        return lastPostUpdatedAt;
    }

    public Long getLastPostUpdatedById() {
        return lastPostUpdatedById;
    }

    public String getLastPostUpdatedBy() {
        return lastPostUpdatedBy;
    }

    public Long getLastPostId() {
        return lastPostId;
    }

    public OffsetDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public String getExcerpt() {
        return excerpt;
    }
}
