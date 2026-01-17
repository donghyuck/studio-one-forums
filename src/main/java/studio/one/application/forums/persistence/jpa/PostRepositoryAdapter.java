package studio.one.application.forums.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import studio.one.application.forums.domain.exception.PostNotFoundException;
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
        PostEntity saved;
        if (post.id() == null) {
            saved = postJpaRepository.save(toEntity(post));
        } else {
            PostEntity entity = postJpaRepository.findById(post.id())
                .orElseThrow(() -> PostNotFoundException.byId(post.id()));
            applyUpdates(entity, post);
            saved = postJpaRepository.save(entity);
        }
        return toDomain(saved);
    }

    @Override
    public List<Post> findByTopicId(Long topicId) {
        return postJpaRepository.findByTopicId(topicId)
            .stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Optional<Post> findById(Long postId) {
        return postJpaRepository.findById(postId).map(this::toDomain);
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
            post.updatedAt(),
            post.deletedAt(),
            post.deletedById(),
            post.hiddenAt(),
            post.hiddenById()
        );
    }

    private void applyUpdates(PostEntity entity, Post post) {
        entity.setContent(post.content());
        entity.setUpdatedById(post.updatedById());
        entity.setUpdatedBy(post.updatedBy());
        entity.setUpdatedAt(post.updatedAt());
        entity.setDeletedAt(post.deletedAt());
        entity.setDeletedById(post.deletedById());
        entity.setHiddenAt(post.hiddenAt());
        entity.setHiddenById(post.hiddenById());
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
            entity.getUpdatedAt(),
            entity.getDeletedAt(),
            entity.getDeletedById(),
            entity.getHiddenAt(),
            entity.getHiddenById(),
            entity.getVersion()
        );
    }
}
