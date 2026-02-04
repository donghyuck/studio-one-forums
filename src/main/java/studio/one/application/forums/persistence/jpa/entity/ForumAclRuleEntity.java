package studio.one.application.forums.persistence.jpa.entity;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import studio.one.application.forums.domain.acl.Effect;
import studio.one.application.forums.domain.acl.IdentifierType;
import studio.one.application.forums.domain.acl.Ownership;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.acl.SubjectType;

@Entity
@Table(name = "tb_application_forum_acl_rule")
public class ForumAclRuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ruleId;

    @Column(name = "board_id", nullable = false)
    private Long forumId;

    private Long categoryId;

    @Column(nullable = false)
    private String role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubjectType subjectType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdentifierType identifierType;

    private Long subjectId;

    private String subjectName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Effect effect;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Ownership ownership;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false)
    private boolean enabled;

    private Long createdById;

    private OffsetDateTime createdAt;

    private Long updatedById;

    private OffsetDateTime updatedAt;

    protected ForumAclRuleEntity() {
    }

    public ForumAclRuleEntity(Long forumId, Long categoryId, String role, SubjectType subjectType,
                              IdentifierType identifierType, Long subjectId, String subjectName,
                              PermissionAction action, Effect effect, Ownership ownership, int priority,
                              boolean enabled, Long createdById, OffsetDateTime createdAt, Long updatedById,
                              OffsetDateTime updatedAt) {
        this.forumId = forumId;
        this.categoryId = categoryId;
        this.role = role;
        this.subjectType = subjectType;
        this.identifierType = identifierType;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.action = action;
        this.effect = effect;
        this.ownership = ownership;
        this.priority = priority;
        this.enabled = enabled;
        this.createdById = createdById;
        this.createdAt = createdAt;
        this.updatedById = updatedById;
        this.updatedAt = updatedAt;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public Long getForumId() {
        return forumId;
    }

    public Long getBoardId() {
        return forumId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getRole() {
        return role;
    }

    public SubjectType getSubjectType() {
        return subjectType;
    }

    public IdentifierType getIdentifierType() {
        return identifierType;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public PermissionAction getAction() {
        return action;
    }

    public Effect getEffect() {
        return effect;
    }

    public Ownership getOwnership() {
        return ownership;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Long getCreatedById() {
        return createdById;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedById() {
        return updatedById;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setSubjectType(SubjectType subjectType) {
        this.subjectType = subjectType;
    }

    public void setIdentifierType(IdentifierType identifierType) {
        this.identifierType = identifierType;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public void setAction(PermissionAction action) {
        this.action = action;
    }

    public void setEffect(Effect effect) {
        this.effect = effect;
    }

    public void setOwnership(Ownership ownership) {
        this.ownership = ownership;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setUpdatedById(Long updatedById) {
        this.updatedById = updatedById;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
