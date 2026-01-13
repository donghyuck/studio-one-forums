package studio.one.application.forums.domain.model;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import studio.one.application.forums.domain.type.TopicStatus;

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
    private Long createdById;
    private String createdBy;
    private OffsetDateTime createdAt;
    private Long updatedById;
    private String updatedBy;
    private OffsetDateTime updatedAt;
    private final long version;

    public void changeStatus(TopicStatus status, Long updatedById, String updatedBy, OffsetDateTime updatedAt) {
        this.status = status;
        this.updatedById = updatedById;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
