package studio.one.application.forums.domain.model;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import studio.one.application.forums.domain.type.TopicStatus;

/**
 * Forums 도메인 모델.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public class Topic {
    private final Long id;
    private final Long forumId;
    private final Long categoryId;
    private String title;
    private List<String> tags;
    private TopicStatus status;
    private boolean pinned;
    private boolean locked;
    private Long createdById;
    private String createdBy;
    private OffsetDateTime createdAt;
    private Long updatedById;
    private String updatedBy;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;
    private Long deletedById;
    private final long version;

    public void changeStatus(TopicStatus status, Long updatedById, String updatedBy, OffsetDateTime updatedAt) {
        this.status = status;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public void setPinned(boolean pinned, Long updatedById, String updatedBy, OffsetDateTime updatedAt) {
        this.pinned = pinned;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public void setLocked(boolean locked, Long updatedById, String updatedBy, OffsetDateTime updatedAt) {
        this.locked = locked;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public void updateContent(String title, List<String> tags, Long updatedById, String updatedBy, OffsetDateTime updatedAt) {
        this.title = title;
        this.tags = tags;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public void softDelete(Long deletedById, OffsetDateTime deletedAt) {
        this.deletedById = deletedById;
        this.deletedAt = deletedAt;
    }
}
