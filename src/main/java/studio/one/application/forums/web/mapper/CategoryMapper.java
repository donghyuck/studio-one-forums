package studio.one.application.forums.web.mapper;

import studio.one.application.forums.service.category.command.CreateCategoryCommand;
import studio.one.application.forums.service.category.query.CategorySummaryView;
import studio.one.application.forums.web.dto.CategoryDtos;

public class CategoryMapper {
    public CreateCategoryCommand toCreateCommand(String forumSlug, CategoryDtos.CreateCategoryRequest request,
                                                 Long createdById, String createdBy) {
        return new CreateCategoryCommand(
            forumSlug,
            request.name,
            request.description,
            request.position,
            createdById,
            createdBy
        );
    }

    public CategoryDtos.CategoryResponse toResponse(CategorySummaryView view) {
        CategoryDtos.CategoryResponse response = new CategoryDtos.CategoryResponse();
        response.id = view.getId();
        response.name = view.getName();
        response.description = view.getDescription();
        response.position = view.getPosition();
        return response;
    }
}
