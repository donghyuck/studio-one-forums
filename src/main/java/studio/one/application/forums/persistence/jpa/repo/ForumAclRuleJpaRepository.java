package studio.one.application.forums.persistence.jpa.repo;

import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.persistence.jpa.entity.ForumAclRuleEntity;

public interface ForumAclRuleJpaRepository extends JpaRepository<ForumAclRuleEntity, Long> {
    List<ForumAclRuleEntity> findByForumId(long forumId);

    @Query("""
        select r
          from ForumAclRuleEntity r
         where r.forumId = :forumId
           and r.action = :action
           and r.enabled = true
           and (
                (:hasRoleNames = true and r.subjectType = 'ROLE' and r.identifierType = 'NAME' and r.subjectName in :roleNames)
                or (:hasRoleIds = true and r.subjectType = 'ROLE' and r.identifierType = 'ID' and r.subjectId in :roleIds)
                or (:hasUserId = true and r.subjectType = 'USER' and r.identifierType = 'ID' and r.subjectId = :userId)
                or (:hasUsername = true and r.subjectType = 'USER' and r.identifierType = 'NAME' and r.subjectName = :username)
           )
           and (
                (:categoryId is null and r.categoryId is null)
                or (:categoryId is not null and (r.categoryId = :categoryId or r.categoryId is null))
           )
        """)
    List<ForumAclRuleEntity> findRules(@Param("forumId") long forumId,
                                       @Param("categoryId") Long categoryId,
                                       @Param("action") PermissionAction action,
                                       @Param("roleNames") Set<String> roleNames,
                                       @Param("roleIds") Set<Long> roleIds,
                                       @Param("userId") Long userId,
                                       @Param("username") String username,
                                       @Param("hasRoleNames") boolean hasRoleNames,
                                       @Param("hasRoleIds") boolean hasRoleIds,
                                       @Param("hasUserId") boolean hasUserId,
                                       @Param("hasUsername") boolean hasUsername);

    @Query("""
        select r
          from ForumAclRuleEntity r
         where r.forumId in :forumIds
           and r.action = :action
           and r.enabled = true
           and (
                (:hasRoleNames = true and r.subjectType = 'ROLE' and r.identifierType = 'NAME' and r.subjectName in :roleNames)
                or (:hasRoleIds = true and r.subjectType = 'ROLE' and r.identifierType = 'ID' and r.subjectId in :roleIds)
                or (:hasUserId = true and r.subjectType = 'USER' and r.identifierType = 'ID' and r.subjectId = :userId)
                or (:hasUsername = true and r.subjectType = 'USER' and r.identifierType = 'NAME' and r.subjectName = :username)
           )
           and (
                (:categoryId is null and r.categoryId is null)
                or (:categoryId is not null and (r.categoryId = :categoryId or r.categoryId is null))
           )
        """)
    List<ForumAclRuleEntity> findRulesBulk(@Param("forumIds") Set<Long> forumIds,
                                           @Param("categoryId") Long categoryId,
                                           @Param("action") PermissionAction action,
                                           @Param("roleNames") Set<String> roleNames,
                                           @Param("roleIds") Set<Long> roleIds,
                                           @Param("userId") Long userId,
                                           @Param("username") String username,
                                           @Param("hasRoleNames") boolean hasRoleNames,
                                           @Param("hasRoleIds") boolean hasRoleIds,
                                           @Param("hasUserId") boolean hasUserId,
                                           @Param("hasUsername") boolean hasUsername);
}
