package studio.one.application.forums.persistence.jpa;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import studio.one.application.forums.domain.model.Post;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.persistence.jpa.entity.PostEntity;
import studio.one.application.forums.persistence.jpa.repo.PostJpaRepository;

/**
 * Forums JPA 영속성 어댑터.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Repository
public class PostRepositoryAdapter implements PostRepository {
    private final PostJpaRepository postJpaRepository;

    public PostRepositoryAdapter(PostJpaRepository postJpaRepository) {
        this.postJpaRepository = postJpaRepository;
    }

    @Override
    public Post save(Post post) {
        PostEntity saved = postJpaRepository.save(toEntity(post));
        return toDomain(saved);
    }

    @Override
    public List<Post> findByTopicId(Long topicId) {
        return postJpaRepository.findByTopicId(topicId)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    private PostEntity toEntity(Post post) {
        return new PostEntity(
            post.topicId(),
            post.content(),
            post.createdById(),
            post.createdBy(),
            post.createdAt(),
            post.updatedById(),
            post.updatedBy(),
            post.updatedAt()
        );
    }

    private Post toDomain(PostEntity entity) {
        return new Post(
            entity.getId(),
            entity.getTopicId(),
            entity.getContent(),
            entity.getCreatedById(),
            entity.getCreatedBy(),
            entity.getCreatedAt(),
            entity.getUpdatedById(),
            entity.getUpdatedBy(),
            entity.getUpdatedAt()
        );
    }
}
