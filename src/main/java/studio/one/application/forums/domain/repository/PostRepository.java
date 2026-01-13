package studio.one.application.forums.domain.repository;

import java.util.List;
import studio.one.application.forums.domain.model.Post;

public interface PostRepository {
    Post save(Post post);

    List<Post> findByTopicId(Long topicId);
}
