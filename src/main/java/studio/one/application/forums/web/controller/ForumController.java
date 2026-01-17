package studio.one.application.forums.web.controller;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.service.forum.ForumQueryService;
import studio.one.application.forums.service.forum.query.ForumDetailView;
import studio.one.application.forums.service.forum.query.ForumSummaryView;
import studio.one.application.forums.web.dto.ForumDtos;
import studio.one.application.forums.web.mapper.ForumMapper;
import studio.one.application.forums.web.authz.ForumAuthz;
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
    private final ForumAuthz forumAuthz;
    private final ForumMapper forumMapper = new ForumMapper();

    public ForumController(ForumQueryService forumQueryService, ForumAuthz forumAuthz) {
        this.forumQueryService = forumQueryService;
        this.forumAuthz = forumAuthz;
    }

    @GetMapping
    @PreAuthorize("@forumAuthz.canListForums('READ_BOARD')")
    public ResponseEntity<ApiResponse<Page<ForumDtos.ForumSummaryResponse>>> listForums(@RequestParam(required = false) String q,
                                                                                        @RequestParam(required = false, name = "in") String inFields,
                                                                                        Pageable pageable) {
        Set<String> inSet = parseCsvSet(inFields);
        ForumAuthz.ForumListVisibility visibility = forumAuthz.listVisibility();
        List<Forum> candidates = forumQueryService.listForumCandidates(
            q, inSet, visibility.isAdmin(), visibility.isMember(), visibility.isSecretListVisible(), visibility.getUserId());
        List<Forum> visibleForums = forumAuthz.filterForumsByAccess(candidates, PermissionAction.READ_BOARD);
        long offset = pageable.getOffset();
        int start = offset >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) offset;
        int end = Math.min(start + pageable.getPageSize(), visibleForums.size());
        List<ForumDtos.ForumSummaryResponse> content = start >= visibleForums.size()
            ? List.of()
            : visibleForums.subList(start, end).stream()
                .map(forum -> forumMapper.toSummaryResponse(new ForumSummaryView(
                    forum.slug().value(),
                    forum.name(),
                    forum.updatedAt()
                )))
                .toList();
        Page<ForumDtos.ForumSummaryResponse> responses = new PageImpl<>(content, pageable, visibleForums.size());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{forumSlug}")
    @PreAuthorize("@forumAuthz.canBoard(#forumSlug, 'READ_BOARD')")
    public ResponseEntity<ApiResponse<ForumDtos.ForumResponse>> getForum(@PathVariable String forumSlug) {
        ForumDetailView view = forumQueryService.getForum(forumSlug);
        return ResponseEntity.ok()
            .eTag(buildEtag(view.getVersion()))
            .body(ApiResponse.ok(forumMapper.toResponse(view)));
    }


    private String buildEtag(long version) {
        return "W/\"" + version + "\"";
    }

    protected static Set<String> parseCsvSet(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }
        Set<String> result = new TreeSet<>();
        Arrays.stream(value.split(","))
            .map(String::trim)
            .filter(item -> !item.isEmpty())
            .map(String::toLowerCase)
            .forEach(result::add);
        return result;
    }

}
