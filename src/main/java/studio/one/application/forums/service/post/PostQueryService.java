package studio.one.application.forums.service.post;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import studio.one.application.forums.domain.exception.TopicNotFoundException;
import studio.one.application.forums.domain.model.Topic;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.persistence.jdbc.PostQueryRepository;
import studio.one.application.forums.service.post.query.PostSummaryView;

@Service
public class PostQueryService {
    private final TopicRepository topicRepository;
    private final PostQueryRepository postQueryRepository;

    public PostQueryService(TopicRepository topicRepository, PostQueryRepository postQueryRepository) {
        this.topicRepository = topicRepository;
        this.postQueryRepository = postQueryRepository;
    }

    public List<PostSummaryView> listPosts(Long topicId, Pageable pageable) {
        Topic topic = topicRepository.findById(topicId)
            .orElseThrow(() -> TopicNotFoundException.byId(topicId));
        return postQueryRepository.findPosts(topic.id(), pageable)
            .stream()
            .map(row -> new PostSummaryView(
                row.getPostId(),
                row.getContent(),
                row.getCreatedById(),
                row.getCreatedBy(),
                row.getCreatedAt()
            ))
            .collect(Collectors.toList());
    }
}
