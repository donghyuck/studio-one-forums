package studio.one.application.forums.web.mapper;

import studio.one.application.forums.service.post.command.CreatePostCommand;
import studio.one.application.forums.service.post.query.PostSummaryView;
import studio.one.application.forums.web.dto.PostDtos;

/**
 * Forums 웹 매퍼.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public class PostMapper {
    public CreatePostCommand toCreateCommand(String forumSlug, Long topicId, PostDtos.CreatePostRequest request,
                                             Long createdById, String createdBy) {
        return new CreatePostCommand(forumSlug, topicId, request.getContent(), createdById, createdBy);
    }

    public PostDtos.PostResponse toResponse(PostSummaryView view) {
        PostDtos.PostResponse response = new PostDtos.PostResponse();
        response.setId(view.getId());
        response.setContent(view.getContent());
        response.setCreatedById(view.getCreatedById());
        response.setCreatedBy(view.getCreatedBy());
        response.setCreatedAt(view.getCreatedAt());
        response.setVersion(view.getVersion());
        return response;
    }
}
