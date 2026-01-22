package studio.one.application.forums.persistence.jdbc;

import java.time.OffsetDateTime;

public class ForumSummaryMetricsRow {
    private final Long forumId;
    private final long topicCount;
    private final long postCount;
    private final OffsetDateTime lastActivityAt;
    private final Long lastActivityById;
    private final String lastActivityBy;
    private final String lastActivityType;
    private final Long lastActivityId;

    public ForumSummaryMetricsRow(Long forumId, long topicCount, long postCount, OffsetDateTime lastActivityAt,
                                  Long lastActivityById, String lastActivityBy, String lastActivityType,
                                  Long lastActivityId) {
        this.forumId = forumId;
        this.topicCount = topicCount;
        this.postCount = postCount;
        this.lastActivityAt = lastActivityAt;
        this.lastActivityById = lastActivityById;
        this.lastActivityBy = lastActivityBy;
        this.lastActivityType = lastActivityType;
        this.lastActivityId = lastActivityId;
    }

    public Long getForumId() {
        return forumId;
    }

    public long getTopicCount() {
        return topicCount;
    }

    public long getPostCount() {
        return postCount;
    }

    public OffsetDateTime getLastActivityAt() {
        return lastActivityAt;
    }

    public Long getLastActivityById() {
        return lastActivityById;
    }

    public String getLastActivityBy() {
        return lastActivityBy;
    }

    public String getLastActivityType() {
        return lastActivityType;
    }

    public Long getLastActivityId() {
        return lastActivityId;
    }
}
