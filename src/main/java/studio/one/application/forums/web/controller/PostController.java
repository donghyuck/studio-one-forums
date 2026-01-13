package studio.one.application.forums.web.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.one.application.forums.service.post.PostCommandService;
import studio.one.application.forums.service.post.PostQueryService;
import studio.one.application.forums.web.dto.PostDtos;
import studio.one.application.forums.web.mapper.PostMapper;
import studio.one.platform.web.dto.ApiResponse;

@RestController
@RequestMapping("/api/forums/{forumSlug}/topics/{topicId}/posts")
public class PostController {
    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;
    private final PostMapper postMapper = new PostMapper();

    public PostController(PostCommandService postCommandService, PostQueryService postQueryService) {
        this.postCommandService = postCommandService;
        this.postQueryService = postQueryService;
    }

    @PostMapping
    @PreAuthorize("@endpointAuthz.can('features:fourms','write')")
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

    @GetMapping
    @PreAuthorize("@endpointAuthz.can('features:fourms','read')")
    public ResponseEntity<ApiResponse<List<PostDtos.PostResponse>>> listPosts(@PathVariable Long topicId, Pageable pageable) {
        List<PostDtos.PostResponse> responses = postQueryService.listPosts(topicId, pageable)
            .stream()
            .map(postMapper::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responses));
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
