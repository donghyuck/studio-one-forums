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
@Table(name = "forums")
public class ForumEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;
 
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

    protected ForumEntity() {
    }

    public ForumEntity(String slug, String name, String description,
                       Long createdById, String createdBy, OffsetDateTime createdAt,
                       Long updatedById, String updatedBy, OffsetDateTime updatedAt) {
        this.slug = slug;
        this.name = name;
        this.description = description;
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

    public void setId(Long id) {
        this.id = id;
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

    public void setVersion(long version) {
        this.version = version;
    }

    public void updateSettings(String name, String description, Long updatedById, String updatedBy, OffsetDateTime updatedAt) {
        this.name = name;
        this.description = description;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
