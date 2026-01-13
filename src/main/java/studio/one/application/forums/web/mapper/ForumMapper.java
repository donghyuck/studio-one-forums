package studio.one.application.forums.web.mapper;

import studio.one.application.forums.service.forum.command.CreateForumCommand;
import studio.one.application.forums.service.forum.command.UpdateForumSettingsCommand;
import studio.one.application.forums.service.forum.query.ForumDetailView;
import studio.one.application.forums.service.forum.query.ForumSummaryView;
import studio.one.application.forums.web.dto.ForumDtos;

public class ForumMapper {
    public CreateForumCommand toCreateCommand(ForumDtos.CreateForumRequest request, Long createdById, String createdBy) {
        return new CreateForumCommand(request.slug, request.name, request.description, createdById, createdBy);
    }

    public UpdateForumSettingsCommand toUpdateCommand(String slug, ForumDtos.UpdateForumSettingsRequest request,
                                                     Long updatedById, String updatedBy, long version) {
        return new UpdateForumSettingsCommand(slug, request.name, request.description, updatedById, updatedBy, version);
    }

    public ForumDtos.ForumResponse toResponse(ForumDetailView view) {
        ForumDtos.ForumResponse response = new ForumDtos.ForumResponse();
        response.id = view.getId();
        response.slug = view.getSlug();
        response.name = view.getName();
        response.description = view.getDescription();
        response.updatedAt = view.getUpdatedAt();
        return response;
    }

    public ForumDtos.ForumSummaryResponse toSummaryResponse(ForumSummaryView view) {
        ForumDtos.ForumSummaryResponse response = new ForumDtos.ForumSummaryResponse();
        response.slug = view.getSlug();
        response.name = view.getName();
        response.updatedAt = view.getUpdatedAt();
        return response;
    }
}
