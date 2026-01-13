package studio.one.application.forums.web.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studio.one.application.forums.service.topic.TopicCommandService;
import studio.one.application.forums.service.topic.TopicQueryService;
import studio.one.application.forums.service.topic.query.TopicDetailView;
import studio.one.application.forums.web.dto.TopicDtos;
import studio.one.application.forums.web.mapper.TopicMapper;
import studio.one.platform.web.dto.ApiResponse;

@RestController
@RequestMapping("/api/mgmt/forums/{forumSlug}")
public class TopicController {
    private final TopicCommandService topicCommandService;
    private final TopicQueryService topicQueryService;
    private final TopicMapper topicMapper = new TopicMapper();

    public TopicController(TopicCommandService topicCommandService, TopicQueryService topicQueryService) {
        this.topicCommandService = topicCommandService;
        this.topicQueryService = topicQueryService;
    }

    @PostMapping("/categories/{categoryId}/topics")
    @PreAuthorize("@endpointAuthz.can('features:fourms','write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTopic(@PathVariable String forumSlug,
                                                                        @PathVariable Long categoryId,
                                                                        @RequestBody TopicDtos.CreateTopicRequest request,
                                                                        @AuthenticationPrincipal(expression = "userId") Long userId,
                                                                        @AuthenticationPrincipal(expression = "username") String username) {
        Long createdById = requireUserId(userId);
        String createdBy = requireUsername(username);
        request.categoryId = categoryId;
        Long topicId = topicCommandService.createTopic(
            topicMapper.toCreateCommand(forumSlug, request, createdById, createdBy)
        ).id();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("topicId", topicId)));
    }

    @GetMapping("/topics")
    @PreAuthorize("@endpointAuthz.can('features:fourms','read')")
    public ResponseEntity<ApiResponse<List<TopicDtos.TopicSummaryResponse>>> listTopics(@PathVariable String forumSlug,
                                                                                        @RequestParam(required = false) String q,
                                                                                        @RequestParam(required = false, name = "in") String inFields,
                                                                                        @RequestParam(required = false) String fields,
                                                                                        Pageable pageable) {
        Set<String> inSet = parseCsvSet(inFields);
        Set<String> fieldSet = parseCsvSet(fields);
        List<TopicDtos.TopicSummaryResponse> responses = topicQueryService.listTopics(forumSlug, q, inSet, fieldSet, pageable)
            .stream()
            .map(topicMapper::toSummaryResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/topics/{topicId}")
    @PreAuthorize("@endpointAuthz.can('features:fourms','read')")
    public ResponseEntity<ApiResponse<TopicDtos.TopicResponse>> getTopic(@PathVariable String forumSlug, @PathVariable Long topicId) {
        TopicDetailView view = topicQueryService.getTopic(forumSlug, topicId);
        return ResponseEntity.ok()
            .eTag(buildEtag(view.getVersion()))
            .body(ApiResponse.ok(topicMapper.toResponse(view)));
    }

    @PatchMapping("/topics/{topicId}/status")
    @PreAuthorize("@endpointAuthz.can('features:fourms','write')")
    public ResponseEntity<ApiResponse<TopicDtos.TopicResponse>> changeStatus(@PathVariable String forumSlug,
                                                                             @PathVariable Long topicId,
                                                                             @RequestBody TopicDtos.ChangeTopicStatusRequest request,
                                                                             @RequestHeader("If-Match") String ifMatch,
                                                                             @AuthenticationPrincipal(expression = "userId") Long userId,
                                                                             @AuthenticationPrincipal(expression = "username") String username) {
        Long updatedById = requireUserId(userId);
        String updatedBy = requireUsername(username);
        long expectedVersion = parseIfMatchVersion(ifMatch);
        topicCommandService.changeStatus(
            topicMapper.toChangeStatusCommand(forumSlug, topicId, request, updatedById, updatedBy, expectedVersion)
        );
        TopicDetailView view = topicQueryService.getTopic(forumSlug, topicId);
        return ResponseEntity.ok()
            .eTag(buildEtag(view.getVersion()))
            .body(ApiResponse.ok(topicMapper.toResponse(view)));
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
