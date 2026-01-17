package studio.one.application.forums.web.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studio.one.application.forums.domain.model.ForumMember;
import studio.one.application.forums.domain.type.ForumMemberRole;
import studio.one.application.forums.service.member.ForumMemberService;
import studio.one.application.forums.web.dto.ForumMemberDtos;
import studio.one.platform.web.dto.ApiResponse;

@RestController
@RequestMapping("${studio.features.forums.web.mgmt-base-path:/api/mgmt/forums}/{forumSlug}/members")
public class ForumMemberAdminController {
    private final ForumMemberService forumMemberService;

    public ForumMemberAdminController(ForumMemberService forumMemberService) {
        this.forumMemberService = forumMemberService;
    }

    @PostMapping
    @PreAuthorize("@forumAuthz.canBoard(#forumSlug, 'MANAGE_BOARD')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addMember(@PathVariable String forumSlug,
                                                                      @RequestBody ForumMemberDtos.UpsertMemberRequest request,
                                                                      @AuthenticationPrincipal(expression = "userId") Long userId) {
        Long actorId = requireUserId(userId);
        forumMemberService.addOrUpdateMemberRole(forumSlug, request.getUserId(), parseRole(request.getRole()), actorId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("userId", request.getUserId())));
    }

    @PatchMapping("/{targetUserId}")
    @PreAuthorize("@forumAuthz.canBoard(#forumSlug, 'MANAGE_BOARD')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateMember(@PathVariable String forumSlug,
                                                                         @PathVariable Long targetUserId,
                                                                         @RequestBody ForumMemberDtos.UpsertMemberRequest request,
                                                                         @AuthenticationPrincipal(expression = "userId") Long userId) {
        Long actorId = requireUserId(userId);
        ForumMemberRole role = parseRole(request.getRole());
        forumMemberService.addOrUpdateMemberRole(forumSlug, targetUserId, role, actorId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("userId", targetUserId)));
    }

    @DeleteMapping("/{targetUserId}")
    @PreAuthorize("@forumAuthz.canBoard(#forumSlug, 'MANAGE_BOARD')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> removeMember(@PathVariable String forumSlug,
                                                                         @PathVariable Long targetUserId,
                                                                         @AuthenticationPrincipal(expression = "userId") Long userId) {
        Long actorId = requireUserId(userId);
        forumMemberService.removeMember(forumSlug, targetUserId, actorId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("userId", targetUserId, "removed", true)));
    }

    @GetMapping
    @PreAuthorize("@forumAuthz.canBoard(#forumSlug, 'MANAGE_BOARD')")
    public ResponseEntity<ApiResponse<List<ForumMemberDtos.MemberResponse>>> listMembers(@PathVariable String forumSlug,
                                                                                          @RequestParam(defaultValue = "0") int page,
                                                                                          @RequestParam(defaultValue = "20") int size) {
        List<ForumMemberDtos.MemberResponse> responses = forumMemberService.listMembers(forumSlug, page, size)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    private ForumMemberDtos.MemberResponse toResponse(ForumMember member) {
        ForumMemberDtos.MemberResponse response = new ForumMemberDtos.MemberResponse();
        response.setUserId(member.userId());
        response.setRole(member.role().name());
        response.setCreatedById(member.createdById());
        response.setCreatedAt(member.createdAt());
        return response;
    }

    private ForumMemberRole parseRole(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("role is required");
        }
        return ForumMemberRole.valueOf(value.trim().toUpperCase());
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        return userId;
    }
}
