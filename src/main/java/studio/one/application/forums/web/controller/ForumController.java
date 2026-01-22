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
import studio.one.application.forums.web.dto.ForumDtos;
import studio.one.application.forums.web.mapper.ForumMapper;
import studio.one.application.forums.web.authz.ForumAuthz;
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
    public ResponseEntity<ApiResponse<Page<ForumDtos.ForumSummaryResponse>>> listForums(@RequestParam(value = "q", required = false) String q,
                                                                                        @RequestParam(required = false, name = "in") String inFields,
                                                                                        Pageable pageable) {
        Set<String> inSet = parseCsvSet(inFields);
        ForumAuthz.ForumListVisibility visibility = forumAuthz.listVisibility();
        Page<ForumDtos.ForumSummaryResponse> responses = listAuthorizedForums(
            q, inSet, visibility, pageable);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{forumSlug}")
    @PreAuthorize("@forumAuthz.canForum(#forumSlug, 'READ_BOARD')")
    public ResponseEntity<ApiResponse<ForumDtos.ForumResponse>> getForum(@PathVariable String forumSlug) {
        ForumDetailView view = forumQueryService.getForum(forumSlug);
        return ResponseEntity.ok()
            .eTag(EtagUtil.buildWeakEtag(view.getVersion()))
            .body(ApiResponse.ok(forumMapper.toResponse(view)));
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

    private Page<ForumDtos.ForumSummaryResponse> listAuthorizedForums(String q, Set<String> inSet,
                                                                      ForumAuthz.ForumListVisibility visibility,
                                                                      Pageable pageable) {
        Page<Forum> candidatePage = forumQueryService.listForumCandidatesPage(
            q, inSet, visibility.isAdmin(), visibility.isMember(), visibility.isSecretListVisible(),
            visibility.getUserId(), pageable);
        List<Forum> allowed = forumAuthz.filterForumsByAccess(candidatePage.getContent(), PermissionAction.READ_BOARD);
        List<Forum> content = new java.util.ArrayList<>(allowed);
        if (content.size() < pageable.getPageSize() && candidatePage.hasNext()) {
            int pageNumber = pageable.getPageNumber() + 1;
            while (content.size() < pageable.getPageSize()) {
                Page<Forum> nextPage = forumQueryService.listForumCandidatesPage(
                    q, inSet, visibility.isAdmin(), visibility.isMember(), visibility.isSecretListVisible(),
                    visibility.getUserId(), org.springframework.data.domain.PageRequest.of(pageNumber, pageable.getPageSize(), pageable.getSort()));
                if (nextPage.isEmpty()) {
                    break;
                }
                List<Forum> nextAllowed = forumAuthz.filterForumsByAccess(nextPage.getContent(), PermissionAction.READ_BOARD);
                content.addAll(nextAllowed);
                if (!nextPage.hasNext()) {
                    break;
                }
                pageNumber += 1;
            }
            if (content.size() > pageable.getPageSize()) {
                content = content.subList(0, pageable.getPageSize());
            }
        }

        long totalAllowed = countAuthorizedForums(q, inSet, visibility, pageable.getSort());
        List<ForumDtos.ForumSummaryResponse> mapped = forumQueryService.summarizeForums(content).stream()
            .map(forumMapper::toSummaryResponse)
            .toList();
        return new PageImpl<>(mapped, pageable, totalAllowed);
    }

    private long countAuthorizedForums(String q, Set<String> inSet, ForumAuthz.ForumListVisibility visibility,
                                       org.springframework.data.domain.Sort sort) {
        int pageSize = 200;
        int page = 0;
        long total = 0;
        while (true) {
            Page<Forum> candidatePage = forumQueryService.listForumCandidatesPage(
                q, inSet, visibility.isAdmin(), visibility.isMember(), visibility.isSecretListVisible(),
                visibility.getUserId(), org.springframework.data.domain.PageRequest.of(page, pageSize, sort));
            if (candidatePage.isEmpty()) {
                break;
            }
            List<Forum> allowed = forumAuthz.filterForumsByAccess(candidatePage.getContent(), PermissionAction.READ_BOARD);
            total += allowed.size();
            if (!candidatePage.hasNext()) {
                break;
            }
            page += 1;
        }
        return total;
    }

}
