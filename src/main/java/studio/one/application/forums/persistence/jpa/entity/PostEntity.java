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
@Table(name = "tb_application_posts")
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

    private OffsetDateTime deletedAt;

    private Long deletedById;

    private OffsetDateTime hiddenAt;

    private Long hiddenById;

    @Version
    private long version;

    protected PostEntity() {
    }

    public PostEntity(Long topicId, String content,
                      Long createdById, String createdBy, OffsetDateTime createdAt,
                      Long updatedById, String updatedBy, OffsetDateTime updatedAt,
                      OffsetDateTime deletedAt, Long deletedById,
                      OffsetDateTime hiddenAt, Long hiddenById) {
        this.topicId = topicId;
        this.content = content;
        this.createdById = createdById;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.deletedById = deletedById;
        this.hiddenAt = hiddenAt;
        this.hiddenById = hiddenById;
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

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }

    public Long getDeletedById() {
        return deletedById;
    }

    public OffsetDateTime getHiddenAt() {
        return hiddenAt;
    }

    public Long getHiddenById() {
        return hiddenById;
    }

    public long getVersion() {
        return version;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUpdatedById(Long updatedById) {
        this.updatedById = updatedById;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeletedAt(OffsetDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public void setDeletedById(Long deletedById) {
        this.deletedById = deletedById;
    }

    public void setHiddenAt(OffsetDateTime hiddenAt) {
        this.hiddenAt = hiddenAt;
    }

    public void setHiddenById(Long hiddenById) {
        this.hiddenById = hiddenById;
    }
}
