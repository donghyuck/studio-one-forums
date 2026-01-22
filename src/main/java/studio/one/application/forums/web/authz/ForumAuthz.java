package studio.one.application.forums.web.authz;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.exception.PostNotFoundException;
import studio.one.application.forums.domain.exception.TopicNotFoundException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.CategoryRepository;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.domain.type.ForumMemberRole;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.service.authz.AuthorizationDecision;
import studio.one.application.forums.service.authz.ForumAccessContext;
import studio.one.application.forums.service.authz.ForumAccessResolver;
import studio.one.application.forums.service.authz.ForumAuthorizer;
import studio.one.application.forums.service.authz.PolicyDecision;

@Component("forumAuthz")
public class ForumAuthz {
    private final ForumRepository forumRepository;
    private final CategoryRepository categoryRepository;
    private final TopicRepository topicRepository;
    private final PostRepository postRepository;
    private final ForumAuthorizer forumAuthorizer;
    private final ForumAccessResolver forumAccessResolver;
    private final boolean secretListVisible;

    public ForumAuthz(ForumRepository forumRepository,
                      CategoryRepository categoryRepository,
                      TopicRepository topicRepository,
                      PostRepository postRepository,
                      ForumAuthorizer forumAuthorizer,
                      ForumAccessResolver forumAccessResolver,
                      @Value("${studio.features.forums.authz.secret-list-visible:false}") boolean secretListVisible) {
        this.forumRepository = forumRepository;
        this.categoryRepository = categoryRepository;
        this.topicRepository = topicRepository;
        this.postRepository = postRepository;
        this.forumAuthorizer = forumAuthorizer;
        this.forumAccessResolver = forumAccessResolver;
        this.secretListVisible = secretListVisible;
    }

    public boolean canListForums(String action) {
        PermissionAction permission = PermissionAction.from(action);
        return canListForums(forumAccessResolver.resolveContext().getRoles(), permission);
    }

    public boolean canListForums(Collection<String> roles, String action) {
        return canListForums(roles, PermissionAction.from(action));
    }

    private boolean canListForums(Collection<String> roles, PermissionAction permission) {
        List<Forum> forums = forumRepository.findAll();
        return !filterForumsByAccess(forums, permission).isEmpty();
    }

    public ForumListVisibility listVisibility() {
        ForumAccessContext context = forumAccessResolver.resolveContext();
        return new ForumListVisibility(forumAccessResolver.isAdmin(context.getRoles()),
            context.isMember(), secretListVisible, context.getUserId());
    }

    public List<Forum> filterForumsByAccess(List<Forum> forums, PermissionAction action) {
        if (forums == null || forums.isEmpty()) {
            return List.of();
        }
        ForumAccessContext context = forumAccessResolver.resolveContext();
        Map<Long, Set<ForumMemberRole>> memberRoles = forumAccessResolver.resolveMemberRoles(context.getUserId());
        Map<Long, PolicyDecision> aclDecisions = forumAuthorizer.aclDecisionsForForums(
            forums.stream().map(Forum::id).toList(), action, context);
        List<Forum> allowed = new java.util.ArrayList<>();
        for (Forum forum : forums) {
            Set<String> effectiveRoles = forumAccessResolver.effectiveRoles(context.getRoles(), memberRoles.get(forum.id()));
            AuthorizationDecision decision = forumAuthorizer.authorizeWithAclDecision(
                forum, null, action, null, false, context, effectiveRoles, aclDecisions.get(forum.id()));
            if (decision.isAllowed()) {
                allowed.add(forum);
            }
        }
        return allowed;
    }

    public boolean canForum(Long forumId, String action) {
        if (forumId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        return forumRepository.findById(forumId)
            .map(forum -> {
                ForumAccessContext context = forumAccessResolver.resolveContext();
                Set<String> effectiveRoles = forumAccessResolver.effectiveRoles(forum.id(), context.getUserId(), context.getRoles());
                return forumAuthorizer.authorize(forum, null, permission, null, false, context, effectiveRoles, true).isAllowed();
            })
            .orElse(false);
    }

    public boolean canForum(Long forumId, Collection<String> roles, String action) {
        if (forumId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        return forumRepository.findById(forumId)
            .map(forum -> {
                ForumAccessContext context = forumAccessResolver.resolveContext();
                Set<String> roleSet = normalizeRoles(roles);
                Set<String> effectiveRoles = forumAccessResolver.effectiveRoles(forum.id(), context.getUserId(), roleSet);
                return forumAuthorizer.authorize(forum, null, permission, null, false, context, effectiveRoles, true).isAllowed();
            })
            .orElse(false);
    }

    public boolean canForum(String forumSlug, String action) {
        if (forumSlug == null || forumSlug.isBlank()) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        return forumRepository.findBySlug(ForumSlug.of(forumSlug))
            .map(forum -> {
                ForumAccessContext context = forumAccessResolver.resolveContext();
                Set<String> effectiveRoles = forumAccessResolver.effectiveRoles(forum.id(), context.getUserId(), context.getRoles());
                return forumAuthorizer.authorize(forum, null, permission, null, false, context, effectiveRoles, true).isAllowed();
            })
            .orElseThrow(() -> ForumNotFoundException.bySlug(forumSlug));
    }

    public boolean canForum(String forumSlug, Collection<String> roles, String action) {
        if (forumSlug == null || forumSlug.isBlank()) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        Set<String> roleSet = normalizeRoles(roles);
        return forumRepository.findBySlug(ForumSlug.of(forumSlug))
            .map(forum -> {
                ForumAccessContext context = forumAccessResolver.resolveContext();
                Set<String> effectiveRoles = forumAccessResolver.effectiveRoles(forum.id(), context.getUserId(), roleSet);
                return forumAuthorizer.authorize(forum, null, permission, null, false, context, effectiveRoles, true).isAllowed();
            })
            .orElseThrow(() -> ForumNotFoundException.bySlug(forumSlug));
    }

    public boolean canCategory(Long categoryId, String action) {
        if (categoryId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        return categoryRepository.findById(categoryId)
            .flatMap(category -> forumRepository.findById(category.forumId())
                .map(forum -> {
                    ForumAccessContext context = forumAccessResolver.resolveContext();
                    Set<String> effectiveRoles = forumAccessResolver.effectiveRoles(forum.id(), context.getUserId(), context.getRoles());
                    return forumAuthorizer.authorize(forum, category.id(), permission, null, false, context, effectiveRoles, true).isAllowed();
                }))
            .orElse(false);
    }

    public boolean canCategory(Long categoryId, Collection<String> roles, String action) {
        if (categoryId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        Set<String> roleSet = normalizeRoles(roles);
        return categoryRepository.findById(categoryId)
            .flatMap(category -> forumRepository.findById(category.forumId())
                .map(forum -> {
                    ForumAccessContext context = forumAccessResolver.resolveContext();
                    Set<String> effectiveRoles = forumAccessResolver.effectiveRoles(forum.id(), context.getUserId(), roleSet);
                    return forumAuthorizer.authorize(forum, category.id(), permission, null, false, context, effectiveRoles, true).isAllowed();
                }))
            .orElse(false);
    }

    public boolean canTopic(Long topicId, String action) {
        if (topicId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        ForumAccessContext context = forumAccessResolver.resolveContext();
        return topicRepository.findById(topicId)
            .flatMap(topic -> forumRepository.findById(topic.forumId())
                .map(forum -> {
                    Set<String> effectiveRoles = forumAccessResolver.effectiveRoles(forum.id(), context.getUserId(), context.getRoles());
                    AuthorizationDecision decision = forumAuthorizer.authorize(forum, topic.categoryId(), permission,
                        topic.createdById(), topic.locked(), context, effectiveRoles, true);
                    return applyTopicAccess(forum, topicId, permission, decision.isAllowed());
                }))
            .orElseThrow(() -> TopicNotFoundException.byId(topicId));
    }

    public boolean canTopic(Long topicId, Collection<String> roles, String action) {
        if (topicId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        Set<String> roleSet = normalizeRoles(roles);
        ForumAccessContext context = forumAccessResolver.resolveContext();
        return topicRepository.findById(topicId)
            .flatMap(topic -> forumRepository.findById(topic.forumId())
                .map(forum -> {
                    Set<String> effectiveRoles = forumAccessResolver.effectiveRoles(forum.id(), context.getUserId(), roleSet);
                    AuthorizationDecision decision = forumAuthorizer.authorize(forum, topic.categoryId(), permission,
                        topic.createdById(), topic.locked(), context, effectiveRoles, true);
                    return applyTopicAccess(forum, topicId, permission, decision.isAllowed());
                }))
            .orElseThrow(() -> TopicNotFoundException.byId(topicId));
    }

    public boolean canPost(Long postId, String action) {
        if (postId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        ForumAccessContext context = forumAccessResolver.resolveContext();
        return canPost(postId, context.getRoles(), permission.name(), context.getUserId());
    }

    public boolean canBoard(Long boardId, String action) {
        return canForum(boardId, action);
    }

    public boolean canBoard(Long boardId, Collection<String> roles, String action) {
        return canForum(boardId, roles, action);
    }

    public boolean canBoard(String forumSlug, String action) {
        return canForum(forumSlug, action);
    }

    public boolean canBoard(String forumSlug, Collection<String> roles, String action) {
        return canForum(forumSlug, roles, action);
    }

    public boolean canPost(Long postId, Collection<String> roles, String action, Long userId) {
        if (postId == null) {
            return false;
        }
        if (userId == null) {
            return false;
        }
        PermissionAction permission = PermissionAction.from(action);
        Set<String> roleSet = normalizeRoles(roles);
        ForumAccessContext context = new ForumAccessContext(roleSet, userId,
            forumAccessResolver.resolveContext().getUsername());
        return postRepository.findById(postId)
            .flatMap(post -> topicRepository.findById(post.topicId())
                .flatMap(topic -> forumRepository.findById(topic.forumId())
                    .map(forum -> {
                        Set<String> effectiveRoles = forumAccessResolver.effectiveRoles(forum.id(), userId, roleSet);
                        AuthorizationDecision decision = forumAuthorizer.authorize(forum, topic.categoryId(), permission,
                            post.createdById(), topic.locked(), context, effectiveRoles, true);
                        return applyPostAccess(forum, postId, permission, decision.isAllowed());
                    })))
            .orElse(false);
    }

    private boolean applyTopicAccess(Forum forum, Long topicId, PermissionAction action, boolean allowed) {
        if (allowed) {
            return true;
        }
        ForumType type = forum.type() != null ? forum.type() : ForumType.COMMON;
        if (type == ForumType.ADMIN_ONLY) {
            throw TopicNotFoundException.byId(topicId);
        }
        if (type == ForumType.SECRET && action == PermissionAction.READ_TOPIC_CONTENT) {
            throw TopicNotFoundException.byId(topicId);
        }
        return false;
    }

    private boolean applyPostAccess(Forum forum, Long postId, PermissionAction action, boolean allowed) {
        if (allowed) {
            return true;
        }
        ForumType type = forum.type() != null ? forum.type() : ForumType.COMMON;
        if (type == ForumType.ADMIN_ONLY) {
            throw PostNotFoundException.byId(postId);
        }
        if (type == ForumType.SECRET && action == PermissionAction.READ_TOPIC_CONTENT) {
            throw PostNotFoundException.byId(postId);
        }
        return false;
    }

    private Set<String> normalizeRoles(Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
            .filter(value -> value != null && !value.isBlank())
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public static class ForumListVisibility {
        private final boolean admin;
        private final boolean member;
        private final boolean secretListVisible;
        private final Long userId;

        public ForumListVisibility(boolean admin, boolean member, boolean secretListVisible, Long userId) {
            this.admin = admin;
            this.member = member;
            this.secretListVisible = secretListVisible;
            this.userId = userId;
        }

        public boolean isAdmin() {
            return admin;
        }

        public boolean isMember() {
            return member;
        }

        public boolean isSecretListVisible() {
            return secretListVisible;
        }

        public Long getUserId() {
            return userId;
        }
    }
}
