package studio.one.application.forums.web.mapper;

import studio.one.application.forums.service.category.command.CreateCategoryCommand;
import studio.one.application.forums.service.category.query.CategorySummaryView;
import studio.one.application.forums.web.dto.CategoryDtos;

/**
 * Forums 웹 매퍼.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public class CategoryMapper {
    public CreateCategoryCommand toCreateCommand(String forumSlug, CategoryDtos.CreateCategoryRequest request,
                                                 Long createdById, String createdBy) {
        return new CreateCategoryCommand(
            forumSlug,
            request.getName(),
            request.getDescription(),
            request.getPosition(),
            createdById,
            createdBy
        );
    }

    public CategoryDtos.CategoryResponse toResponse(CategorySummaryView view) {
        CategoryDtos.CategoryResponse response = new CategoryDtos.CategoryResponse();
        response.setId(view.getId());
        response.setName(view.getName());
        response.setDescription(view.getDescription());
        response.setPosition(view.getPosition());
        return response;
    }
}
