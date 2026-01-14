package studio.one.application.forums.persistence.jpa.entity;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Forums JPA 엔티티.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Entity
@Table(name = "posts")
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long topicId;

    @Column(nullable = false, length = 8000)
    private String content;
 
    @Column(nullable = false)
    private Long createdById;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private OffsetDateTime createdAt;
 
    @Column(nullable = false)
    private Long updatedById;

    @Column(nullable = false)
    private String updatedBy;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private long version;

    protected PostEntity() {
    }

    public PostEntity(Long topicId, String content,
                      Long createdById, String createdBy, OffsetDateTime createdAt,
                      Long updatedById, String updatedBy, OffsetDateTime updatedAt) {
        this.topicId = topicId;
        this.content = content;
        this.createdById = createdById;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getTopicId() {
        return topicId;
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

    public Long getUpdatedById() {
        return updatedById;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}
