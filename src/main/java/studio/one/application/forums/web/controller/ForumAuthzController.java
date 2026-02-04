package studio.one.application.forums.web.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.service.permission.ForumPermissionService;
import studio.one.application.forums.web.authz.ForumAuthz;
import studio.one.application.forums.web.dto.ForumAuthzDtos;
import studio.one.application.forums.web.dto.ForumPermissionDtos;
import studio.one.platform.web.dto.ApiResponse;

/**
 * 공개 포럼 뷰에서 자체 권한 상태를 위한 API를 제공합니다.
 */
@RestController
@RequestMapping("${studio.features.forums.web.base-path:/api/forums}")
public class ForumAuthzController {
    private final ForumAuthz forumAuthz;
    private final ForumPermissionService permissionService;

    public ForumAuthzController(ForumAuthz forumAuthz, ForumPermissionService permissionService) {
        this.forumAuthz = forumAuthz;
        this.permissionService = permissionService;
    }

    @GetMapping("/{forumSlug}/authz")
    @PreAuthorize("@forumAuthz.canForum(#forumSlug, 'READ_BOARD')")
    public ResponseEntity<ApiResponse<List<ForumAuthzDtos.ActionPermission>>> actions(
            @PathVariable String forumSlug) {
        List<ForumAuthzDtos.ActionPermission> rows = Arrays.stream(PermissionAction.values())
            .map(action -> {
                ForumAuthzDtos.ActionPermission dto = new ForumAuthzDtos.ActionPermission();
                dto.setAction(action.name());
                dto.setAllowed(forumAuthz.canForum(forumSlug, action.name()));
                return dto;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(rows));
    }

    @GetMapping("/{forumSlug}/authz/simulate")
    @PreAuthorize("@forumAuthz.canForum(#forumSlug, 'READ_BOARD')")
    public ResponseEntity<ApiResponse<ForumPermissionDtos.SimulationResponse>> simulate(
            @PathVariable String forumSlug,
            @RequestParam String action,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(defaultValue = "false") boolean locked,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String username) {
        ForumPermissionDtos.SimulationResponse response = permissionService.simulate(
                forumSlug, action, role, categoryId, ownerId, locked, userId, username);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
