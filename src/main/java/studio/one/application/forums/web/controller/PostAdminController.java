package studio.one.application.forums.web.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.one.application.forums.service.post.PostCommandService;
import studio.one.application.forums.web.dto.PostDtos;
import studio.one.application.forums.web.mapper.PostMapper;
import studio.one.platform.web.dto.ApiResponse;

/**
 * Forums 관리자 REST 컨트롤러.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@RestController
@RequestMapping("${studio.features.forums.web.mgmt-base-path:/api/mgmt/forums}/{forumSlug}/topics/{topicId}/posts")
public class PostAdminController {
    private final PostCommandService postCommandService;
    private final PostMapper postMapper = new PostMapper();

    public PostAdminController(PostCommandService postCommandService) {
        this.postCommandService = postCommandService;
    }

    @PostMapping
    @PreAuthorize("@forumsAuthz.canCreatePost(#forumSlug, #topicId)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createPost(@PathVariable String forumSlug,
                                                                       @PathVariable Long topicId,
                                                                       @RequestBody PostDtos.CreatePostRequest request,
                                                                       @AuthenticationPrincipal(expression = "userId") Long userId,
                                                                       @AuthenticationPrincipal(expression = "username") String username) {
        Long createdById = requireUserId(userId);
        String createdBy = requireUsername(username);
        Long postId = postCommandService.createPost(
            postMapper.toCreateCommand(forumSlug, topicId, request, createdById, createdBy)
        ).id();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("postId", postId)));
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
