package studio.one.application.forums.web.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studio.one.application.forums.service.topic.TopicQueryService;
import studio.one.application.forums.service.topic.query.TopicDetailView;
import studio.one.application.forums.web.dto.TopicDtos;
import studio.one.application.forums.web.mapper.TopicMapper;
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
    private final TopicMapper topicMapper = new TopicMapper();

    public TopicController(TopicQueryService topicQueryService) {
        this.topicQueryService = topicQueryService;
    }

    @GetMapping("/topics")
    @PreAuthorize("@forumsAuthz.canListTopics(#forumSlug)")
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
    @PreAuthorize("@forumsAuthz.canReadTopic(#forumSlug, #topicId)")
    public ResponseEntity<ApiResponse<TopicDtos.TopicResponse>> getTopic(@PathVariable String forumSlug, @PathVariable Long topicId) {
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
}
