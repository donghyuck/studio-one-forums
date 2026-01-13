package studio.one.application.forums.web.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import studio.one.application.forums.service.forum.ForumCommandService;
import studio.one.application.forums.service.forum.ForumQueryService;
import studio.one.application.forums.service.forum.query.ForumDetailView;
import studio.one.application.forums.web.dto.ForumDtos;
import studio.one.application.forums.web.mapper.ForumMapper;
import studio.one.platform.web.dto.ApiResponse;

@RestController
@RequestMapping("/api/forums")
public class ForumController {
    private final ForumCommandService forumCommandService;
    private final ForumQueryService forumQueryService;
    private final ForumMapper forumMapper = new ForumMapper();

    public ForumController(ForumCommandService forumCommandService, ForumQueryService forumQueryService) {
        this.forumCommandService = forumCommandService;
        this.forumQueryService = forumQueryService;
    }

    @PostMapping
    @PreAuthorize("@endpointAuthz.can('features:fourms','write')")
    public ResponseEntity<ApiResponse<ForumDtos.ForumResponse>> createForum(
        @RequestBody ForumDtos.CreateForumRequest request,
        @AuthenticationPrincipal(expression = "userId") Long userId,
        @AuthenticationPrincipal(expression = "username") String username
    ) {
        Long createdById = requireUserId(userId);
        String createdBy = requireUsername(username);
        forumCommandService.createForum(forumMapper.toCreateCommand(request, createdById, createdBy));
        ForumDetailView view = forumQueryService.getForum(request.slug);
        return ResponseEntity
            .ok()
            .eTag(buildEtag(view.getVersion()))
            .body(ApiResponse.ok(forumMapper.toResponse(view)));
    }

    @GetMapping
    @PreAuthorize("@endpointAuthz.can('features:fourms','read')")
    public ResponseEntity<ApiResponse<List<ForumDtos.ForumSummaryResponse>>> listForums() {
        List<ForumDtos.ForumSummaryResponse> responses = forumQueryService.listForums()
            .stream()
            .map(forumMapper::toSummaryResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{forumSlug}")
    @PreAuthorize("@endpointAuthz.can('features:fourms','read')")
    public ResponseEntity<ApiResponse<ForumDtos.ForumResponse>> getForum(@PathVariable String forumSlug) {
        ForumDetailView view = forumQueryService.getForum(forumSlug);
        return ResponseEntity.ok()
            .eTag(buildEtag(view.getVersion()))
            .body(ApiResponse.ok(forumMapper.toResponse(view)));
    }

    @PutMapping("/{forumSlug}/settings")
    @PreAuthorize("@endpointAuthz.can('features:fourms','write')")
    public ResponseEntity<ApiResponse<ForumDtos.ForumResponse>> updateSettings(
        @PathVariable String forumSlug,
        @RequestBody ForumDtos.UpdateForumSettingsRequest request,
        @RequestHeader("If-Match") String ifMatch,
        @AuthenticationPrincipal(expression = "userId") Long userId,
        @AuthenticationPrincipal(expression = "username") String username
    ) {
        Long updatedById = requireUserId(userId);
        String updatedBy = requireUsername(username);
        long expectedVersion = parseIfMatchVersion(ifMatch);
        forumCommandService.updateSettings(forumMapper.toUpdateCommand(forumSlug, request, updatedById, updatedBy, expectedVersion));
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
