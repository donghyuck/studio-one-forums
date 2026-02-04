package studio.one.application.forums.web.controller;

import java.util.List;

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

import studio.one.application.forums.service.permission.ForumPermissionService;
import studio.one.application.forums.web.dto.ForumPermissionDtos;
import studio.one.application.forums.web.dto.PermissionActionDtos;
import studio.one.platform.web.dto.ApiResponse;

/**
 * 관리자가 포럼 단위 ACL 룰, PermissionAction 메타, 시뮬레이션을 사용할 수 있도록 REST API를 제공합니다.
 */
@RestController  
@RequestMapping("${studio.features.forums.web.mgmt-base-path:/api/mgmt/forums}/{forumSlug}/permissions")
public class ForumPermissionController {
    private final ForumPermissionService permissionService;

    public ForumPermissionController(ForumPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping("/actions")
    @PreAuthorize("@forumAuthz.canForum(#forumSlug, 'MANAGE_BOARD')")
    /**
     * 등록 가능한 PermissionAction 전체 목록을 반환합니다. UI에서 드롭다운/테이블을 구성할 때 사용합니다.
     */
    public ResponseEntity<ApiResponse<List<PermissionActionDtos.ActionResponse>>> actions(@PathVariable String forumSlug) {
        return ResponseEntity.ok(ApiResponse.ok(PermissionActionDtos.all()));
    }

    @GetMapping
    @PreAuthorize("@forumAuthz.canForum(#forumSlug, 'MANAGE_BOARD')")
    /**
     * 지정된 포럼/카테고리에 적용된 ACL 룰을 목록화합니다. priority 기준으로 정렬되어 UI에 그대로 노출 가능합니다.
     */
    public ResponseEntity<ApiResponse<List<ForumPermissionDtos.RuleResponse>>> list(@PathVariable String forumSlug,
            @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(ApiResponse.ok(permissionService.listRules(forumSlug, categoryId)));
    }

    @PostMapping
    @PreAuthorize("@forumAuthz.canForum(#forumSlug, 'MANAGE_BOARD')")
    /**
     * 새로운 ACL 룰을 생성하고, 생성자 정보를 감사 필드로 저장합니다.
     */
    public ResponseEntity<ApiResponse<ForumPermissionDtos.RuleResponse>> create(@PathVariable String forumSlug,
            @RequestBody ForumPermissionDtos.RuleRequest request,
            @AuthenticationPrincipal(expression = "userId") Long userId) {
        Long actorId = requireUserId(userId);
        ForumPermissionDtos.RuleResponse created = permissionService.create(forumSlug, request, actorId);
        return ResponseEntity.ok(ApiResponse.ok(created));
    }

    @PatchMapping("/{ruleId}")
    @PreAuthorize("@forumAuthz.canForum(#forumSlug, 'MANAGE_BOARD')")
    /**
     * 기존 ACL 룰을 업데이트합니다.
     */
    public ResponseEntity<ApiResponse<ForumPermissionDtos.RuleResponse>> update(@PathVariable String forumSlug,
            @PathVariable Long ruleId,
            @RequestBody ForumPermissionDtos.RuleRequest request,
            @AuthenticationPrincipal(expression = "userId") Long userId) {
        Long actorId = requireUserId(userId);
        ForumPermissionDtos.RuleResponse updated = permissionService.update(forumSlug, ruleId, request, actorId);
        return ResponseEntity.ok(ApiResponse.ok(updated));
    }

    @DeleteMapping("/{ruleId}")
    @PreAuthorize("@forumAuthz.canForum(#forumSlug, 'MANAGE_BOARD')")
    /**
     * 해당 ruleId에 해당하는 ACL 룰을 삭제합니다.
     */
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String forumSlug,
            @PathVariable Long ruleId,
            @AuthenticationPrincipal(expression = "userId") Long userId) {
        Long actorId = requireUserId(userId);
        permissionService.delete(forumSlug, ruleId, actorId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/check")
    @PreAuthorize("@forumAuthz.canForum(#forumSlug, 'MANAGE_BOARD')")
    /**
     * role/user/owner/locked 조건으로 policy+ACL 결과를 시뮬레이션해서 변경 전 영향을 확인할 수 있습니다.
     */
    public ResponseEntity<ApiResponse<ForumPermissionDtos.SimulationResponse>> simulate(@PathVariable String forumSlug,
            @RequestParam String action,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(defaultValue = "false") boolean locked,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username) {
        ForumPermissionDtos.SimulationResponse response = permissionService.simulate(forumSlug, action, role,
                categoryId, ownerId, locked, userId, username);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        return userId;
    }
}
