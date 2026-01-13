package studio.one.application.forums.persistence.jdbc;

import java.time.OffsetDateTime;

public class TopicListRow {
    private final Long topicId;
    private final String title;
    private final String status;
    private final OffsetDateTime updatedAt;

    public TopicListRow(Long topicId, String title, String status, OffsetDateTime updatedAt) {
        this.topicId = topicId;
        this.title = title;
        this.status = status;
        this.updatedAt = updatedAt;
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
}
