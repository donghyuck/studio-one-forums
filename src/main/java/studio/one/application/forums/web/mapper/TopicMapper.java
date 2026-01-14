package studio.one.application.forums.web.mapper;

import studio.one.application.forums.domain.type.TopicStatus;
import studio.one.application.forums.service.topic.command.ChangeTopicStatusCommand;
import studio.one.application.forums.service.topic.command.CreateTopicCommand;
import studio.one.application.forums.service.topic.query.TopicDetailView;
import studio.one.application.forums.service.topic.query.TopicSummaryView;
import studio.one.application.forums.web.dto.TopicDtos;

/**
 * Forums 웹 매퍼.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public class TopicMapper {
    public CreateTopicCommand toCreateCommand(String forumSlug, TopicDtos.CreateTopicRequest request,
                                              Long createdById, String createdBy) {
        return new CreateTopicCommand(
            forumSlug,
            request.getCategoryId(),
            request.getTitle(),
            request.getTags(),
            createdById,
            createdBy
        );
    }

    public ChangeTopicStatusCommand toChangeStatusCommand(String forumSlug, Long topicId,
                                                          TopicDtos.ChangeTopicStatusRequest request,
                                                          Long updatedById, String updatedBy, long version) {
        return new ChangeTopicStatusCommand(
            forumSlug,
            topicId,
            TopicStatus.valueOf(request.getStatus()),
            updatedById,
            updatedBy,
            version
        );
    }

    public TopicDtos.TopicResponse toResponse(TopicDetailView view) {
        TopicDtos.TopicResponse response = new TopicDtos.TopicResponse();
        response.setId(view.getId());
        response.setCategoryId(view.getCategoryId());
        response.setTitle(view.getTitle());
        response.setTags(view.getTags());
        response.setStatus(view.getStatus());
        response.setUpdatedAt(view.getUpdatedAt());
        return response;
    }

    public TopicDtos.TopicSummaryResponse toSummaryResponse(TopicSummaryView view) {
        TopicDtos.TopicSummaryResponse response = new TopicDtos.TopicSummaryResponse();
        response.setId(view.getId());
        response.setTitle(view.getTitle());
        response.setStatus(view.getStatus());
        response.setUpdatedAt(view.getUpdatedAt());
        return response;
    }
}
