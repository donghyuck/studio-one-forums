package studio.one.application.forums.web.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studio.one.application.forums.service.post.PostQueryService;
import studio.one.application.forums.web.dto.PostDtos;
import studio.one.application.forums.web.mapper.PostMapper;
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
    private final PostMapper postMapper = new PostMapper();

    public PostController(PostQueryService postQueryService) {
        this.postQueryService = postQueryService;
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
}
