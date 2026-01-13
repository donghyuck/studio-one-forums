package studio.one.application.forums.web.mapper;

import studio.one.application.forums.service.post.command.CreatePostCommand;
import studio.one.application.forums.service.post.query.PostSummaryView;
import studio.one.application.forums.web.dto.PostDtos;

public class PostMapper {
    public CreatePostCommand toCreateCommand(String forumSlug, Long topicId, PostDtos.CreatePostRequest request,
                                             Long createdById, String createdBy) {
        return new CreatePostCommand(forumSlug, topicId, request.content, createdById, createdBy);
    }

    public PostDtos.PostResponse toResponse(PostSummaryView view) {
        PostDtos.PostResponse response = new PostDtos.PostResponse();
        response.id = view.getId();
        response.content = view.getContent();
        response.createdById = view.getCreatedById();
        response.createdBy = view.getCreatedBy();
        response.createdAt = view.getCreatedAt();
        return response;
    }
}
