package studio.one.application.forums.web.controller;

import static org.springframework.http.ResponseEntity.ok;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import studio.one.application.forums.domain.type.ForumAuditEntityType;
import studio.one.application.forums.service.audit.ForumAuditLogQueryService;
import studio.one.application.forums.service.audit.query.ForumAuditLogQuery;
import studio.one.application.forums.service.forum.ForumQueryService;
import studio.one.application.forums.web.dto.ForumAuditLogDtos;
import studio.one.application.forums.web.mapper.ForumAuditLogMapper;
import studio.one.platform.web.dto.ApiResponse;

/**
 * Forums 감사 로그 관리자 REST 컨트롤러.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-02-05  Son Donghyuck  최초 생성
 * </pre>
 */
@RestController
@RequestMapping("${studio.features.forums.web.mgmt-base-path:/api/mgmt/forums}/audit-logs")
public class ForumAuditLogMgmtController {

    private final ForumAuditLogQueryService queryService;
    private final ForumQueryService forumQueryService;
    private final ForumAuditLogMapper mapper = new ForumAuditLogMapper();

    public ForumAuditLogMgmtController(ForumAuditLogQueryService queryService, ForumQueryService forumQueryService) {
        this.queryService = queryService;
        this.forumQueryService = forumQueryService;
    }

    @GetMapping
    @PreAuthorize("@endpointAuthz.can('features:forums','read')")
    public ResponseEntity<ApiResponse<Page<ForumAuditLogDtos.AuditLogResponse>>> listAuditLogs(
            @RequestParam(required = false) Long forumId,
            @RequestParam(required = false) String forumSlug,
            @RequestParam(required = false) ForumAuditEntityType entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime to,
            Pageable pageable) {
        OffsetDateTime truncated = to;
        if (truncated != null) {
            truncated = to.truncatedTo(ChronoUnit.DAYS).plusDays(1);
        }
        Long resolvedForumId = resolveForumId(forumId, forumSlug);
        ForumAuditLogQuery query = new ForumAuditLogQuery(
            resolvedForumId,
            entityType != null ? entityType.name() : null,
            entityId,
            actorId,
            from,
            truncated
        );
        Page<ForumAuditLogDtos.AuditLogResponse> responses = queryService.find(query, pageable)
            .map(mapper::toResponse);
        return ok(ApiResponse.ok(responses));
    }

    private Long resolveForumId(Long forumId, String forumSlug) {
        if (forumSlug == null || forumSlug.isBlank()) {
            return forumId;
        }
        Long resolved = forumQueryService.getForum(forumSlug.trim()).getId();
        if (forumId != null && !forumId.equals(resolved)) {
            throw new IllegalArgumentException("forumId does not match forumSlug");
        }
        return resolved;
    }
}
