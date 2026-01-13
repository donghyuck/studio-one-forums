package studio.one.application.forums.web.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.one.application.forums.service.category.CategoryCommandService;
import studio.one.application.forums.service.category.CategoryQueryService;
import studio.one.application.forums.web.dto.CategoryDtos;
import studio.one.application.forums.web.mapper.CategoryMapper;
import studio.one.platform.web.dto.ApiResponse;

@RestController
@RequestMapping("/api/forums/{forumSlug}/categories")
public class CategoryController {
    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;
    private final CategoryMapper categoryMapper = new CategoryMapper();

    public CategoryController(CategoryCommandService categoryCommandService, CategoryQueryService categoryQueryService) {
        this.categoryCommandService = categoryCommandService;
        this.categoryQueryService = categoryQueryService;
    }

    @PostMapping
    @PreAuthorize("@endpointAuthz.can('features:fourms','write')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCategory(
        @PathVariable String forumSlug,
        @RequestBody CategoryDtos.CreateCategoryRequest request,
        @AuthenticationPrincipal(expression = "userId") Long userId,
        @AuthenticationPrincipal(expression = "username") String username
    ) {
        Long createdById = requireUserId(userId);
        String createdBy = requireUsername(username);
        Long categoryId = categoryCommandService.createCategory(
            categoryMapper.toCreateCommand(forumSlug, request, createdById, createdBy)
        ).id();
        return ResponseEntity.ok(ApiResponse.ok(Map.of("categoryId", categoryId)));
    }

    @GetMapping
    @PreAuthorize("@endpointAuthz.can('features:fourms','read')")
    public ResponseEntity<ApiResponse<List<CategoryDtos.CategoryResponse>>> listCategories(@PathVariable String forumSlug) {
        List<CategoryDtos.CategoryResponse> responses = categoryQueryService.listCategories(forumSlug)
            .stream()
            .map(categoryMapper::toResponse)
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