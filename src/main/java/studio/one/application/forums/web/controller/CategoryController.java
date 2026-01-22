package studio.one.application.forums.web.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.one.application.forums.service.category.CategoryQueryService;
import studio.one.application.forums.web.dto.CategoryDtos;
import studio.one.application.forums.web.mapper.CategoryMapper;
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
@RequestMapping("${studio.features.forums.web.base-path:/api/forums}/{forumSlug}/categories")
public class CategoryController {
    private final CategoryQueryService categoryQueryService;
    private final CategoryMapper categoryMapper = new CategoryMapper();

    public CategoryController(CategoryQueryService categoryQueryService) {
        this.categoryQueryService = categoryQueryService;
    }

    @GetMapping
    @PreAuthorize("@forumAuthz.canForum(#forumSlug, 'READ_BOARD')")
    public ResponseEntity<ApiResponse<List<CategoryDtos.CategoryResponse>>> listCategories(@PathVariable String forumSlug) {
        List<CategoryDtos.CategoryResponse> responses = categoryQueryService.listCategories(forumSlug)
            .stream()
            .map(categoryMapper::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}
