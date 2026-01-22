package studio.one.application.forums.web.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studio.one.application.forums.service.post.PostCommandService;
import studio.one.application.forums.service.post.PostQueryService;
import studio.one.application.forums.service.post.command.DeletePostCommand;
import studio.one.application.forums.web.dto.PostDtos;
import studio.one.application.forums.web.mapper.PostMapper;
import studio.one.application.forums.web.etag.EtagUtil;
import studio.one.platform.web.dto.ApiResponse;

/**
 * Forums REST 컨트롤러.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@RestController
@RequestMapping("${studio.features.forums.web.base-path:/api/forums}/{forumSlug}/topics/{topicId}/posts")
public class PostController {
    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;
    private final PostMapper postMapper = new PostMapper();

    public PostController(PostQueryService postQueryService, PostCommandService postCommandService) {
        this.postQueryService = postQueryService;
        this.postCommandService = postCommandService;
    }

    @GetMapping
    @PreAuthorize("@forumAuthz.canTopic(#topicId, 'READ_TOPIC_CONTENT') and (!#includeDeleted || @forumAuthz.canTopic(#topicId, 'MODERATE')) and (!#includeHidden || @forumAuthz.canTopic(#topicId, 'MODERATE'))")
    public ResponseEntity<ApiResponse<List<PostDtos.PostResponse>>> listPosts(@PathVariable String forumSlug,
                                                                             @PathVariable Long topicId,
                                                                             @RequestParam(required = false, defaultValue = "false") boolean includeDeleted,
                                                                             @RequestParam(required = false, defaultValue = "false") boolean includeHidden,
                                                                             Pageable pageable) {
        List<PostDtos.PostResponse> responses = postQueryService.listPosts(topicId, pageable, includeDeleted, includeHidden)
            .stream()
            .map(postMapper::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responses));
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

    @PatchMapping("/{postId}")
    @PreAuthorize("@forumAuthz.canPost(#postId, 'EDIT_POST')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updatePost(@PathVariable String forumSlug,
                                                                       @PathVariable Long topicId,
                                                                       @PathVariable Long postId,
                                                                       @RequestBody PostDtos.UpdatePostRequest request,
                                                                       @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                                                       @AuthenticationPrincipal(expression = "userId") Long userId,
                                                                       @AuthenticationPrincipal(expression = "username") String username) {
        Long updatedById = requireUserId(userId);
        String updatedBy = requireUsername(username);
        long expectedVersion = EtagUtil.parseIfMatchVersion(ifMatch);
        postCommandService.updatePost(postMapper.toUpdateCommand(postId, request, updatedById, updatedBy, expectedVersion));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("postId", postId, "updated", true)));
    }

    @DeleteMapping("/{postId}")
    @PreAuthorize("@forumAuthz.canPost(#postId, 'DELETE_POST')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deletePost(@PathVariable String forumSlug,
                                                                       @PathVariable Long topicId,
                                                                       @PathVariable Long postId,
                                                                       @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                                                       @AuthenticationPrincipal(expression = "userId") Long userId,
                                                                       @AuthenticationPrincipal(expression = "username") String username) {
        Long deletedById = requireUserId(userId);
        String deletedBy = requireUsername(username);
        long expectedVersion = EtagUtil.parseIfMatchVersion(ifMatch);
        postCommandService.deletePost(new DeletePostCommand(postId, deletedById, deletedBy, expectedVersion));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("postId", postId, "deleted", true)));
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
