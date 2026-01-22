package studio.one.application.forums.web.controller;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
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

import studio.one.application.forums.service.topic.TopicQueryService;
import studio.one.application.forums.service.topic.TopicCommandService;
import studio.one.application.forums.service.topic.query.TopicDetailView;
import studio.one.application.forums.service.topic.command.DeleteTopicCommand;
import studio.one.application.forums.web.dto.TopicDtos;
import studio.one.application.forums.web.mapper.TopicMapper;
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
@RequestMapping("${studio.features.forums.web.base-path:/api/forums}/{forumSlug}")
public class TopicController {
    private final TopicQueryService topicQueryService;
    private final TopicCommandService topicCommandService;
    private final TopicMapper topicMapper = new TopicMapper();

    public TopicController(TopicQueryService topicQueryService, TopicCommandService topicCommandService) {
        this.topicQueryService = topicQueryService;
        this.topicCommandService = topicCommandService;
    }

    @GetMapping("/topics")
    @PreAuthorize("@forumAuthz.canForum(#forumSlug, 'READ_TOPIC_LIST')")
    public ResponseEntity<ApiResponse<Page<TopicDtos.TopicSummaryResponse>>> listTopics(@PathVariable String forumSlug,
                                                                                        @RequestParam(value = "q", required = false) String q,
                                                                                        @RequestParam(required = false, name = "in") String inFields,
                                                                                        Pageable pageable) {
        Set<String> inSet = parseCsvSet(inFields);
        Page<TopicDtos.TopicSummaryResponse> responses = topicQueryService.listTopics(forumSlug, q, inSet, pageable)
            .map(topicMapper::toSummaryResponse);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PostMapping("/categories/{categoryId}/topics")
    @PreAuthorize("@forumAuthz.canCategory(#categoryId, 'CREATE_TOPIC')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTopic(@PathVariable String forumSlug,
                                                                        @PathVariable Long categoryId,
                                                                        @RequestBody TopicDtos.CreateTopicRequest request,
                                                                        @AuthenticationPrincipal(expression = "userId") Long userId,
                                                                        @AuthenticationPrincipal(expression = "username") String username) {
        Long createdById = requireUserId(userId);
        String createdBy = requireUsername(username);
        request.setCategoryId(categoryId);
        Long topicId = topicCommandService.createTopic(
            topicMapper.toCreateCommand(forumSlug, request, createdById, createdBy)
        ).id();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("topicId", topicId)));
    }

    @PostMapping("/topics")
    @PreAuthorize("@forumAuthz.canForum(#forumSlug, 'CREATE_TOPIC')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTopicWithoutCategory(@PathVariable String forumSlug,
                                                                                       @RequestBody TopicDtos.CreateTopicRequest request,
                                                                                       @AuthenticationPrincipal(expression = "userId") Long userId,
                                                                                       @AuthenticationPrincipal(expression = "username") String username) {
        if (request.getCategoryId() != null) {
            throw new IllegalArgumentException("categoryId is not allowed in this endpoint");
        }
        Long createdById = requireUserId(userId);
        String createdBy = requireUsername(username);
        Long topicId = topicCommandService.createTopic(
            topicMapper.toCreateCommand(forumSlug, request, createdById, createdBy)
        ).id();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("topicId", topicId)));
    }

    @GetMapping("/topics/{topicId}")
    @PreAuthorize("@forumAuthz.canTopic(#topicId, 'READ_TOPIC_CONTENT')")
    public ResponseEntity<ApiResponse<TopicDtos.TopicResponse>> getTopic(@PathVariable String forumSlug,
                                                                         @PathVariable Long topicId,
                                                                         @RequestParam(required = false) String include) {
        TopicDetailView view = topicQueryService.getTopic(forumSlug, topicId);
        return ResponseEntity.ok()
            .eTag(EtagUtil.buildWeakEtag(view.getVersion()))
            .body(ApiResponse.ok(topicMapper.toResponse(view)));
    }

    @PatchMapping("/topics/{topicId}")
    @PreAuthorize("@forumAuthz.canTopic(#topicId, 'EDIT_TOPIC')")
    public ResponseEntity<ApiResponse<TopicDtos.TopicResponse>> updateTopic(@PathVariable String forumSlug,
                                                                            @PathVariable Long topicId,
                                                                            @RequestBody TopicDtos.UpdateTopicRequest request,
                                                                            @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                                                            @AuthenticationPrincipal(expression = "userId") Long userId,
                                                                            @AuthenticationPrincipal(expression = "username") String username) {
        Long updatedById = requireUserId(userId);
        String updatedBy = requireUsername(username);
        long expectedVersion = EtagUtil.parseIfMatchVersion(ifMatch);
        topicCommandService.updateTopic(
            topicMapper.toUpdateCommand(forumSlug, topicId, request, updatedById, updatedBy, expectedVersion)
        );
        TopicDetailView view = topicQueryService.getTopic(forumSlug, topicId);
        return ResponseEntity.ok()
            .eTag(EtagUtil.buildWeakEtag(view.getVersion()))
            .body(ApiResponse.ok(topicMapper.toResponse(view)));
    }

    @DeleteMapping("/topics/{topicId}")
    @PreAuthorize("@forumAuthz.canTopic(#topicId, 'DELETE_TOPIC')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteTopic(@PathVariable String forumSlug,
                                                                        @PathVariable Long topicId,
                                                                        @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                                                        @AuthenticationPrincipal(expression = "userId") Long userId,
                                                                        @AuthenticationPrincipal(expression = "username") String username) {
        Long deletedById = requireUserId(userId);
        String deletedBy = requireUsername(username);
        long expectedVersion = EtagUtil.parseIfMatchVersion(ifMatch);
        topicCommandService.deleteTopic(new DeleteTopicCommand(forumSlug, topicId, deletedById, deletedBy, expectedVersion));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("topicId", topicId, "deleted", true)));
    }

    private Set<String> parseCsvSet(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(item -> !item.isEmpty())
            .collect(Collectors.toSet());
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
