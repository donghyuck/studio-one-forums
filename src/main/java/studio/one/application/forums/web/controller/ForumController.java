package studio.one.application.forums.web.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.one.application.forums.service.forum.ForumQueryService;
import studio.one.application.forums.service.forum.query.ForumDetailView;
import studio.one.application.forums.web.dto.ForumDtos;
import studio.one.application.forums.web.mapper.ForumMapper;
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
@RequestMapping("${studio.features.forums.web.base-path:/api/forums}")
public class ForumController {
    private final ForumQueryService forumQueryService;
    private final ForumMapper forumMapper = new ForumMapper();

    public ForumController(ForumQueryService forumQueryService) {
        this.forumQueryService = forumQueryService;
    }

    @GetMapping
    @PreAuthorize("@forumsAuthz.canListForums()")
    public ResponseEntity<ApiResponse<List<ForumDtos.ForumSummaryResponse>>> listForums() {
        List<ForumDtos.ForumSummaryResponse> responses = forumQueryService.listForums()
            .stream()
            .map(forumMapper::toSummaryResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{forumSlug}")
    @PreAuthorize("@forumsAuthz.canReadForum(#forumSlug)")
    public ResponseEntity<ApiResponse<ForumDtos.ForumResponse>> getForum(@PathVariable String forumSlug) {
        ForumDetailView view = forumQueryService.getForum(forumSlug);
        return ResponseEntity.ok()
            .eTag(buildEtag(view.getVersion()))
            .body(ApiResponse.ok(forumMapper.toResponse(view)));
    }


    private String buildEtag(long version) {
        return "W/\"" + version + "\"";
    }

}
