package studio.one.application.forums.web.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.one.application.forums.service.post.PostCommandService;
import studio.one.application.forums.service.post.command.DeletePostCommand;
import studio.one.application.forums.service.post.command.HidePostCommand;
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
    @PreAuthorize("@forumAuthz.canTopic(#topicId, 'REPLY_POST')")
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

    @PatchMapping("/{postId}/hide")
    @PreAuthorize("@forumAuthz.canPost(#postId, 'HIDE_POST')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> hidePost(@PathVariable String forumSlug,
                                                                     @PathVariable Long topicId,
                                                                     @PathVariable Long postId,
                                                                     @RequestBody PostDtos.HidePostRequest request,
                                                                     @RequestHeader("If-Match") String ifMatch,
                                                                     @AuthenticationPrincipal(expression = "userId") Long userId,
                                                                     @AuthenticationPrincipal(expression = "username") String username) {
        Long updatedById = requireUserId(userId);
        String updatedBy = requireUsername(username);
        long expectedVersion = parseIfMatchVersion(ifMatch);
        postCommandService.hidePost(new HidePostCommand(postId, request.isHidden(), request.getReason(),
            updatedById, updatedBy, expectedVersion));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("postId", postId, "hidden", request.isHidden())));
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("@forumAuthz.canPost(#postId, 'DELETE_POST')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deletePost(@PathVariable String forumSlug,
                                                                       @PathVariable Long topicId,
                                                                       @PathVariable Long postId,
                                                                       @RequestHeader("If-Match") String ifMatch,
                                                                       @AuthenticationPrincipal(expression = "userId") Long userId,
                                                                       @AuthenticationPrincipal(expression = "username") String username) {
        Long deletedById = requireUserId(userId);
        String deletedBy = requireUsername(username);
        long expectedVersion = parseIfMatchVersion(ifMatch);
        postCommandService.deletePost(new DeletePostCommand(postId, deletedById, deletedBy, expectedVersion));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("postId", postId, "deleted", true)));
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
