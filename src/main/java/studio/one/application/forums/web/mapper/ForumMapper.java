package studio.one.application.forums.web.mapper;

import studio.one.application.forums.service.forum.command.CreateForumCommand;
import studio.one.application.forums.service.forum.command.UpdateForumSettingsCommand;
import studio.one.application.forums.service.forum.query.ForumDetailView;
import studio.one.application.forums.service.forum.query.ForumSummaryView;
import studio.one.application.forums.web.dto.ForumDtos;

/**
 * Forums 웹 매퍼.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public class ForumMapper {
    public CreateForumCommand toCreateCommand(ForumDtos.CreateForumRequest request, Long createdById, String createdBy) {
        return new CreateForumCommand(request.getSlug(), request.getName(), request.getDescription(), createdById, createdBy);
    }

    public UpdateForumSettingsCommand toUpdateCommand(String slug, ForumDtos.UpdateForumSettingsRequest request,
                                                     Long updatedById, String updatedBy, long version) {
        return new UpdateForumSettingsCommand(slug, request.getName(), request.getDescription(), updatedById, updatedBy, version);
    }

    public ForumDtos.ForumResponse toResponse(ForumDetailView view) {
        ForumDtos.ForumResponse response = new ForumDtos.ForumResponse();
        response.setId(view.getId());
        response.setSlug(view.getSlug());
        response.setName(view.getName());
        response.setDescription(view.getDescription());
        response.setUpdatedAt(view.getUpdatedAt());
        return response;
    }

    public ForumDtos.ForumSummaryResponse toSummaryResponse(ForumSummaryView view) {
        ForumDtos.ForumSummaryResponse response = new ForumDtos.ForumSummaryResponse();
        response.setSlug(view.getSlug());
        response.setName(view.getName());
        response.setUpdatedAt(view.getUpdatedAt());
        return response;
    }
}
