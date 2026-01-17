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
public class PostListRow {
    
    private final Long postId;
    private final String content;
    private final Long createdById;
    private final String createdBy;
    private final OffsetDateTime createdAt;
    private final long version;

    public PostListRow(Long postId, String content, Long createdById, String createdBy, OffsetDateTime createdAt,
                       long version) {
        this.postId = postId;
        this.content = content;
        this.createdById = createdById;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.version = version;
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

    public long getVersion() {
        return version;
    }
}
