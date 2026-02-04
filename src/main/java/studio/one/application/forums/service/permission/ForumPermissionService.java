package studio.one.application.forums.service.permission;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import studio.one.application.forums.domain.acl.Effect;
import studio.one.application.forums.domain.acl.ForumAclRule;
import studio.one.application.forums.domain.acl.IdentifierType;
import studio.one.application.forums.domain.acl.Ownership;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.acl.SubjectType;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.ForumAclRuleRepository;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.service.authz.AuthorizationDecision;
import studio.one.application.forums.service.authz.ForumAccessContext;
import studio.one.application.forums.service.authz.ForumAuthorizer;
import studio.one.application.forums.web.dto.ForumPermissionDtos;

@Service
@RequiredArgsConstructor 
public class ForumPermissionService {

    private final ForumRepository forumRepository;
    private final ForumAclRuleRepository aclRuleRepository;
    private final ForumAuthorizer forumAuthorizer;

    public List<ForumPermissionDtos.RuleResponse> listRules(String forumSlug, Long categoryId) {
        Forum forum = resolveForum(forumSlug);
        return aclRuleRepository.findByForumId(forum.id()).stream()
                .filter(rule -> categoryId == null
                        || (rule.categoryId() != null && rule.categoryId().equals(categoryId)))
                .sorted(Comparator.comparing((ForumAclRule rule) -> rule.categoryId() == null ? Long.MAX_VALUE
                                : rule.categoryId())
                        .thenComparing(Comparator.comparingInt(ForumAclRule::priority).reversed()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ForumPermissionDtos.RuleResponse create(String forumSlug, ForumPermissionDtos.RuleRequest request, Long actorId) {
        Forum forum = resolveForum(forumSlug);
        OffsetDateTime now = OffsetDateTime.now();
        normalizeSubject(request);
        ForumAclRule rule = buildRule(forum, request, actorId, now, now);
        return toResponse(aclRuleRepository.save(rule));
    }

    @Transactional
    public ForumPermissionDtos.RuleResponse update(String forumSlug, Long ruleId, ForumPermissionDtos.RuleRequest request,
            Long actorId) {
        Forum forum = resolveForum(forumSlug);
        OffsetDateTime now = OffsetDateTime.now();
        ForumAclRule existing = aclRuleRepository.findById(ruleId)
                .filter(rule -> rule.forumId().equals(forum.id()))
                .orElseThrow(() -> new IllegalArgumentException("rule not found: " + ruleId));
        normalizeSubject(request);
        ForumAclRule updated = mergeRule(existing, request, actorId, now);
        return toResponse(aclRuleRepository.save(updated));
    }

    @Transactional
    public void delete(String forumSlug, Long ruleId, Long actorId) {
        Forum forum = resolveForum(forumSlug);
        ForumAclRule existing = aclRuleRepository.findById(ruleId)
                .filter(rule -> rule.forumId().equals(forum.id()))
                .orElseThrow(() -> new IllegalArgumentException("rule not found: " + ruleId));
        aclRuleRepository.delete(existing);
    }

    public ForumPermissionDtos.SimulationResponse simulate(String forumSlug, String action, String role,
            Long categoryId, Long ownerId, boolean locked, Long userId, String username) {
        Forum forum = resolveForum(forumSlug);
        PermissionAction permissionAction = PermissionAction.from(action);
        String normalizedRole = role == null ? "MEMBER" : role.trim().toUpperCase(Locale.ROOT);
        Set<String> roles = Collections.singleton(normalizedRole);
        ForumAccessContext context = new ForumAccessContext(roles, userId, username);
        AuthorizationDecision decision = forumAuthorizer.authorize(forum, categoryId, permissionAction, ownerId, locked,
                context, roles, true);
        return buildSimulationResponse(permissionAction, normalizedRole, categoryId, decision);
    }

    private Forum resolveForum(String slug) {
        return forumRepository.findBySlug(ForumSlug.of(slug))
                .orElseThrow(() -> new IllegalArgumentException("forum not found: " + slug));
    }

    private ForumPermissionDtos.RuleResponse toResponse(ForumAclRule rule) {
        ForumPermissionDtos.RuleResponse response = new ForumPermissionDtos.RuleResponse();
        response.setRuleId(rule.ruleId());
        response.setCategoryId(rule.categoryId());
        response.setRole(rule.role());
        response.setSubjectType(rule.subjectType());
        response.setIdentifierType(rule.identifierType());
        response.setSubjectId(rule.subjectId());
        response.setSubjectName(rule.subjectName());
        response.setAction(rule.action());
        response.setEffect(rule.effect());
        response.setOwnership(rule.ownership());
        response.setPriority(rule.priority());
        response.setEnabled(rule.enabled());
        response.setCreatedById(rule.createdById());
        response.setCreatedAt(rule.createdAt());
        response.setUpdatedById(rule.updatedById());
        response.setUpdatedAt(rule.updatedAt());
        return response;
    }

    private void normalizeSubject(ForumPermissionDtos.RuleRequest request) {
        if (request.getSubjectType() == null && request.getRole() != null) {
            request.setSubjectType(SubjectType.ROLE);
        }
        if (request.getSubjectType() == SubjectType.ROLE) {
            request.setIdentifierType(IdentifierType.NAME);
            if (request.getSubjectName() == null) {
                request.setSubjectName(request.getRole());
            }
        }
    }

    private ForumAclRule buildRule(Forum forum, ForumPermissionDtos.RuleRequest request, Long actorId,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        SubjectType subjectType = request.getSubjectType() != null ? request.getSubjectType() : SubjectType.ROLE;
        IdentifierType identifierType = request.getIdentifierType() != null ? request.getIdentifierType() : IdentifierType.NAME;
        String role = request.getRole();
        Long subjectId = request.getSubjectId();
        String subjectName = resolveSubjectName(subjectType, identifierType, request.getSubjectName(), role);
        return new ForumAclRule(
                null,
                forum.id(),
                request.getCategoryId(),
                subjectType,
                identifierType,
                subjectId,
                subjectName,
                role,
                request.getAction() != null ? request.getAction() : PermissionAction.READ_BOARD,
                request.getEffect() != null ? request.getEffect() : Effect.ALLOW,
                request.getOwnership() != null ? request.getOwnership() : Ownership.ANY,
                request.getPriority(),
                request.isEnabled(),
                actorId,
                createdAt,
                actorId,
                updatedAt
        );
    }

    private ForumAclRule mergeRule(ForumAclRule existing, ForumPermissionDtos.RuleRequest request, Long actorId,
            OffsetDateTime now) {
        SubjectType subjectType = request.getSubjectType() != null ? request.getSubjectType() : existing.subjectType();
        IdentifierType identifierType = request.getIdentifierType() != null ? request.getIdentifierType()
                : existing.identifierType();
        String role = request.getRole() != null ? request.getRole() : existing.role();
        Long subjectId = request.getSubjectId() != null ? request.getSubjectId() : existing.subjectId();
        String subjectName = resolveSubjectName(subjectType, identifierType,
                request.getSubjectName() != null ? request.getSubjectName() : existing.subjectName(), role);
        return new ForumAclRule(
                existing.ruleId(),
                existing.forumId(),
                request.getCategoryId() != null ? request.getCategoryId() : existing.categoryId(),
                subjectType,
                identifierType,
                subjectId,
                subjectName,
                role,
                request.getAction() != null ? request.getAction() : existing.action(),
                request.getEffect() != null ? request.getEffect() : existing.effect(),
                request.getOwnership() != null ? request.getOwnership() : existing.ownership(),
                request.getPriority(),
                request.isEnabled(),
                existing.createdById(),
                existing.createdAt(),
                actorId,
                now
        );
    }

    private String resolveSubjectName(SubjectType subjectType, IdentifierType identifierType, String subjectName,
            String role) {
        if (subjectName != null) {
            return subjectName;
        }
        if (subjectType == SubjectType.ROLE && identifierType == IdentifierType.NAME) {
            return role;
        }
        return null;
    }

    private ForumPermissionDtos.SimulationResponse buildSimulationResponse(PermissionAction action, String role,
            Long categoryId, AuthorizationDecision decision) {
        ForumPermissionDtos.SimulationResponse response = new ForumPermissionDtos.SimulationResponse();
        response.setAction(action.name());
        response.setRole(role);
        response.setCategoryId(categoryId);
        response.setAllowed(decision.isAllowed());
        response.setPolicyDecision(decision.getPolicyDecision());
        response.setAclDecision(decision.getAclDecision());
        response.setDenyReason(decision.getDenyReason());
        if (decision.isAllowed()) {
            response.setMessage("Allowed by policy/ACL");
        } else if (decision.getDenyReason() != null) {
            response.setMessage("Denied due to " + decision.getDenyReason().name());
        } else {
            response.setMessage("Denied");
        }
        return response;
    }
}
