package studio.one.application.forums.web.mapper;

import studio.one.application.forums.domain.type.TopicStatus;
import studio.one.application.forums.service.topic.command.ChangeTopicStatusCommand;
import studio.one.application.forums.service.topic.command.CreateTopicCommand;
import studio.one.application.forums.service.topic.query.TopicDetailView;
import studio.one.application.forums.service.topic.query.TopicSummaryView;
import studio.one.application.forums.web.dto.TopicDtos;

public class TopicMapper {
    public CreateTopicCommand toCreateCommand(String forumSlug, TopicDtos.CreateTopicRequest request,
                                              Long createdById, String createdBy) {
        return new CreateTopicCommand(
            forumSlug,
            request.categoryId,
            request.title,
            request.tags,
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
            TopicStatus.valueOf(request.status),
            updatedById,
            updatedBy,
            version
        );
    }

    public TopicDtos.TopicResponse toResponse(TopicDetailView view) {
        TopicDtos.TopicResponse response = new TopicDtos.TopicResponse();
        response.id = view.getId();
        response.categoryId = view.getCategoryId();
        response.title = view.getTitle();
        response.tags = view.getTags();
        response.status = view.getStatus();
        response.updatedAt = view.getUpdatedAt();
        return response;
    }

    public TopicDtos.TopicSummaryResponse toSummaryResponse(TopicSummaryView view) {
        TopicDtos.TopicSummaryResponse response = new TopicDtos.TopicSummaryResponse();
        response.id = view.getId();
        response.title = view.getTitle();
        response.status = view.getStatus();
        response.updatedAt = view.getUpdatedAt();
        return response;
    }
}
