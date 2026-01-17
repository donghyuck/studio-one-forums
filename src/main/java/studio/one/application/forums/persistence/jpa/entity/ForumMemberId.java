package studio.one.application.forums.persistence.jpa.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ForumMemberId implements Serializable {
    @Column(name = "forum_id", nullable = false)
    private Long forumId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    protected ForumMemberId() {
    }

    public ForumMemberId(Long forumId, Long userId) {
        this.forumId = forumId;
        this.userId = userId;
    }

    public Long getForumId() {
        return forumId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForumMemberId)) return false;
        ForumMemberId that = (ForumMemberId) o;
        return forumId.equals(that.forumId) && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(forumId, userId);
    }
}
