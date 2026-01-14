package studio.one.application.forums.persistence.jpa;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.persistence.jpa.entity.ForumEntity;
import studio.one.application.forums.persistence.jpa.repo.ForumJpaRepository;

/**
 * Forums JPA 영속성 어댑터.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Repository
public class ForumRepositoryAdapter implements ForumRepository {
    private final ForumJpaRepository forumJpaRepository;

    public ForumRepositoryAdapter(ForumJpaRepository forumJpaRepository) {
        this.forumJpaRepository = forumJpaRepository;
    }

    @Override
    public Forum save(Forum forum) {
        ForumEntity entity = toEntity(forum);
        ForumEntity saved = forumJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Forum> findById(Long forumId) {
        return forumJpaRepository.findById(forumId).map(this::toDomain);
    }

    @Override
    public Optional<Forum> findBySlug(ForumSlug slug) {
        return forumJpaRepository.findBySlug(slug.value()).map(this::toDomain);
    }

    @Override
    public boolean existsBySlug(ForumSlug slug) {
        return forumJpaRepository.findBySlug(slug.value()).isPresent();
    }

    @Override
    public java.util.List<Forum> findAll() {
        return forumJpaRepository.findAll()
            .stream()
            .map(this::toDomain)
            .collect(java.util.stream.Collectors.toList());
    }

    private ForumEntity toEntity(Forum forum) {
        ForumEntity entity = new ForumEntity(
            forum.slug().value(),
            forum.name(),
            forum.description(),
            forum.createdById(),
            forum.createdBy(),
            forum.createdAt(),
            forum.updatedById(),
            forum.updatedBy(),
            forum.updatedAt()
        );
        if (forum.id() != null) {
            entity.setId(forum.id());
            entity.setVersion(forum.version());
        }
        return entity;
    }

    private Forum toDomain(ForumEntity entity) {
        return new Forum(
            entity.getId(),
            ForumSlug.of(entity.getSlug()),
            entity.getName(),
            entity.getDescription(),
            entity.getCreatedById(),
            entity.getCreatedBy(),
            entity.getCreatedAt(),
            entity.getUpdatedById(),
            entity.getUpdatedBy(),
            entity.getUpdatedAt(),
            entity.getVersion()
        );
    }
}
