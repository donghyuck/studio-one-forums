package studio.one.application.forums.domain.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import studio.one.application.forums.domain.vo.ForumSlug;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public class Forum {
    private final Long id;
    private final ForumSlug slug;
    private String name;
    private String description;
    private Long createdById;
    private String createdBy;
    private OffsetDateTime createdAt;
    private Long updatedById;
    private String updatedBy;
    private OffsetDateTime updatedAt;
    private final long version;

    public void updateSettings(String name, String description, Long updatedById, String updatedBy, OffsetDateTime updatedAt) {
        this.name = name;
        this.description = description;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
