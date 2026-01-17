package studio.one.application.forums.persistence.jpa.entity;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import studio.one.application.forums.domain.type.ForumMemberRole;

@Entity
@Table(name = "tb_application_forum_member")
public class ForumMemberEntity {
    @EmbeddedId
    private ForumMemberId id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ForumMemberRole role;

    private Long createdById;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    protected ForumMemberEntity() {
    }

    public ForumMemberEntity(ForumMemberId id, ForumMemberRole role, Long createdById, OffsetDateTime createdAt) {
        this.id = id;
        this.role = role;
        this.createdById = createdById;
        this.createdAt = createdAt;
    }

    public ForumMemberId getId() {
        return id;
    }

    public ForumMemberRole getRole() {
        return role;
    }

    public void setRole(ForumMemberRole role) {
        this.role = role;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
