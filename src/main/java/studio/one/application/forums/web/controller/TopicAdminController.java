package studio.one.application.forums.web.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.one.application.forums.service.topic.TopicCommandService;
import studio.one.application.forums.service.topic.TopicQueryService;
import studio.one.application.forums.service.topic.query.TopicDetailView;
import studio.one.application.forums.service.topic.command.DeleteTopicCommand;
import studio.one.application.forums.service.topic.command.LockTopicCommand;
import studio.one.application.forums.service.topic.command.PinTopicCommand;
import studio.one.application.forums.web.dto.TopicDtos;
import studio.one.application.forums.web.mapper.TopicMapper;
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
@RequestMapping("${studio.features.forums.web.mgmt-base-path:/api/mgmt/forums}/{forumSlug}")
public class TopicAdminController {
    private final TopicCommandService topicCommandService;
    private final TopicQueryService topicQueryService;
    private final TopicMapper topicMapper = new TopicMapper();

    public TopicAdminController(TopicCommandService topicCommandService, TopicQueryService topicQueryService) {
        this.topicCommandService = topicCommandService;
        this.topicQueryService = topicQueryService;
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

    @PatchMapping("/topics/{topicId}/status")
    @PreAuthorize("@forumAuthz.canTopic(#topicId, 'MODERATE')")
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

    @PatchMapping("/topics/{topicId}/pin")
    @PreAuthorize("@forumAuthz.canTopic(#topicId, 'PIN_TOPIC')")
    public ResponseEntity<ApiResponse<TopicDtos.TopicResponse>> pinTopic(@PathVariable String forumSlug,
                                                                         @PathVariable Long topicId,
                                                                         @RequestBody TopicDtos.PinTopicRequest request,
                                                                         @RequestHeader("If-Match") String ifMatch,
                                                                         @AuthenticationPrincipal(expression = "userId") Long userId,
                                                                         @AuthenticationPrincipal(expression = "username") String username) {
        Long updatedById = requireUserId(userId);
        String updatedBy = requireUsername(username);
        long expectedVersion = parseIfMatchVersion(ifMatch);
        topicCommandService.pinTopic(new PinTopicCommand(forumSlug, topicId, request.isPinned(), updatedById, updatedBy, expectedVersion));
        TopicDetailView view = topicQueryService.getTopic(forumSlug, topicId);
        return ResponseEntity.ok()
            .eTag(buildEtag(view.getVersion()))
            .body(ApiResponse.ok(topicMapper.toResponse(view)));
    }

    @PatchMapping("/topics/{topicId}/lock")
    @PreAuthorize("@forumAuthz.canTopic(#topicId, 'LOCK_TOPIC')")
    public ResponseEntity<ApiResponse<TopicDtos.TopicResponse>> lockTopic(@PathVariable String forumSlug,
                                                                          @PathVariable Long topicId,
                                                                          @RequestBody TopicDtos.LockTopicRequest request,
                                                                          @RequestHeader("If-Match") String ifMatch,
                                                                          @AuthenticationPrincipal(expression = "userId") Long userId,
                                                                          @AuthenticationPrincipal(expression = "username") String username) {
        Long updatedById = requireUserId(userId);
        String updatedBy = requireUsername(username);
        long expectedVersion = parseIfMatchVersion(ifMatch);
        topicCommandService.lockTopic(new LockTopicCommand(forumSlug, topicId, request.isLocked(), updatedById, updatedBy, expectedVersion));
        TopicDetailView view = topicQueryService.getTopic(forumSlug, topicId);
        return ResponseEntity.ok()
            .eTag(buildEtag(view.getVersion()))
            .body(ApiResponse.ok(topicMapper.toResponse(view)));
    }

    @DeleteMapping("/topics/{topicId}")
    @PreAuthorize("@forumAuthz.canTopic(#topicId, 'DELETE_TOPIC')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deleteTopic(@PathVariable String forumSlug,
                                                                        @PathVariable Long topicId,
                                                                        @RequestHeader("If-Match") String ifMatch,
                                                                        @AuthenticationPrincipal(expression = "userId") Long userId,
                                                                        @AuthenticationPrincipal(expression = "username") String username) {
        Long deletedById = requireUserId(userId);
        String deletedBy = requireUsername(username);
        long expectedVersion = parseIfMatchVersion(ifMatch);
        topicCommandService.deleteTopic(new DeleteTopicCommand(forumSlug, topicId, deletedById, deletedBy, expectedVersion));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("topicId", topicId, "deleted", true)));
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
