package studio.one.application.forums.service.topic.query;

import java.time.OffsetDateTime;

public class TopicSummaryView {
    private final Long id;
    private final String title;
    private final String status;
    private final OffsetDateTime updatedAt;

    public TopicSummaryView(Long id, String title, String status, OffsetDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
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
