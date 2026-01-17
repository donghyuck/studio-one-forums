package studio.one.application.forums.web.controller;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import studio.one.application.forums.service.forum.ForumCommandService;
import studio.one.application.forums.service.forum.ForumQueryService;
import studio.one.application.forums.service.forum.query.ForumDetailView;
import studio.one.application.forums.web.dto.ForumDtos;
import studio.one.application.forums.web.mapper.ForumMapper;
import studio.one.platform.web.dto.ApiResponse;

/**
 * Forums 관리자 REST 컨트롤러.
 *
 * <p>
 * 개정이력
 * </p>
 * 
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@RestController
@RequestMapping("${studio.features.forums.web.mgmt-base-path:/api/mgmt/forums}")
public class ForumAdminController {
    private final ForumCommandService forumCommandService;
    private final ForumQueryService forumQueryService;
    private final ForumMapper forumMapper = new ForumMapper();

    public ForumAdminController(ForumCommandService forumCommandService, ForumQueryService forumQueryService) {
        this.forumCommandService = forumCommandService;
        this.forumQueryService = forumQueryService;
    }

    @GetMapping
    @PreAuthorize("@endpointAuthz.can('features:forums','read')")
    public ResponseEntity<ApiResponse<Page<ForumDtos.ForumSummaryResponse>>> listForums(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, name = "in") String inFields,
            Pageable pageable) {
        Set<String> inSet = ForumController.parseCsvSet(inFields);
        Page<ForumDtos.ForumSummaryResponse> responses = forumQueryService.listForums(q, inSet, pageable)
                .map(forumMapper::toSummaryResponse);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{forumSlug}")
    @PreAuthorize("@endpointAuthz.can('features:forums','read')")
    public ResponseEntity<ApiResponse<ForumDtos.ForumResponse>> getForum(@PathVariable String forumSlug) {
        ForumDetailView view = forumQueryService.getForum(forumSlug);
        return ResponseEntity.ok()
                .eTag(buildEtag(view.getVersion()))
                .body(ApiResponse.ok(forumMapper.toResponse(view)));
    }

    @PostMapping
   @PreAuthorize("@endpointAuthz.can('features:forums','write')")
    public ResponseEntity<ApiResponse<ForumDtos.ForumResponse>> createForum(
            @RequestBody ForumDtos.CreateForumRequest request,
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @AuthenticationPrincipal(expression = "username") String username) {
        Long createdById = requireUserId(userId);
        String createdBy = requireUsername(username);
        forumCommandService.createForum(forumMapper.toCreateCommand(request, createdById, createdBy));
        ForumDetailView view = forumQueryService.getForum(request.getSlug());
        return ResponseEntity
                .ok()
                .eTag(buildEtag(view.getVersion()))
                .body(ApiResponse.ok(forumMapper.toResponse(view)));
    }

    @PutMapping("/{forumSlug}/settings")
    @PreAuthorize("@endpointAuthz.can('features:forums','write')")
    public ResponseEntity<ApiResponse<ForumDtos.ForumResponse>> updateSettings(
            @PathVariable String forumSlug,
            @RequestBody ForumDtos.UpdateForumSettingsRequest request,
            @RequestHeader("If-Match") String ifMatch,
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @AuthenticationPrincipal(expression = "username") String username) {
        Long updatedById = requireUserId(userId);
        String updatedBy = requireUsername(username);
        long expectedVersion = parseIfMatchVersion(ifMatch);
        forumCommandService.updateSettings(
                forumMapper.toUpdateCommand(forumSlug, request, updatedById, updatedBy, expectedVersion));
        ForumDetailView view = forumQueryService.getForum(forumSlug);
        return ResponseEntity.ok()
                .eTag(buildEtag(view.getVersion()))
                .body(ApiResponse.ok(forumMapper.toResponse(view)));
    }

    private String buildEtag(long version) {
        return "W/\"" + version + "\"";
    }

    private long parseIfMatchVersion(String ifMatch) {
        String token = ifMatch.replace("W/", "").replace("\"", "").trim();
        return Long.parseLong(token);
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        return userId;
    }

    private String requireUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        return username;
    }
}
