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
@Table(name = "tb_application_topics")
public class TopicEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long forumId;

    @Column
    private Long categoryId;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String tags;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private boolean pinned;

    @Column(nullable = false)
    private boolean locked;
 
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

    private OffsetDateTime deletedAt;

    private Long deletedById;

    @Version
    private long version;

    protected TopicEntity() {
    }

    public TopicEntity(Long forumId, Long categoryId, String title, String tags, String status,
                       boolean pinned, boolean locked,
                       Long createdById, String createdBy, OffsetDateTime createdAt,
                       Long updatedById, String updatedBy, OffsetDateTime updatedAt,
                       OffsetDateTime deletedAt, Long deletedById) {
        this.forumId = forumId;
        this.categoryId = categoryId;
        this.title = title;
        this.tags = tags;
        this.status = status;
        this.pinned = pinned;
        this.locked = locked;
        this.createdById = createdById;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.deletedById = deletedById;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getForumId() {
        return forumId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getTags() {
        return tags;
    }

    public String getStatus() {
        return status;
    }

    public boolean isPinned() {
        return pinned;
    }

    public boolean isLocked() {
        return locked;
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

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public Long getDeletedById() {
        return deletedById;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public void updateStatus(String status, Long updatedById, String updatedBy, OffsetDateTime updatedAt) {
        this.status = status;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public void updatePinned(boolean pinned, Long updatedById, String updatedBy, OffsetDateTime updatedAt) {
        this.pinned = pinned;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public void updateLocked(boolean locked, Long updatedById, String updatedBy, OffsetDateTime updatedAt) {
        this.locked = locked;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public void softDelete(Long deletedById, OffsetDateTime deletedAt) {
        this.deletedById = deletedById;
        this.deletedAt = deletedAt;
    }
}
