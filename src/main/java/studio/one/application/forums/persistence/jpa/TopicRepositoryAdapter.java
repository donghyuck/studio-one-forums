package studio.one.application.forums.persistence.jpa;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import studio.one.application.forums.domain.model.Topic;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.domain.type.TopicStatus;
import studio.one.application.forums.persistence.jpa.entity.TopicEntity;
import studio.one.application.forums.persistence.jpa.repo.TopicJpaRepository;

/**
 * Forums JPA 영속성 어댑터.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Repository
public class TopicRepositoryAdapter implements TopicRepository {
    private final TopicJpaRepository topicJpaRepository;

    public TopicRepositoryAdapter(TopicJpaRepository topicJpaRepository) {
        this.topicJpaRepository = topicJpaRepository;
    }

    @Override
    public Topic save(Topic topic) {
        TopicEntity saved = topicJpaRepository.save(toEntity(topic));
        return toDomain(saved);
    }

    @Override
    public Optional<Topic> findById(Long topicId) {
        return topicJpaRepository.findById(topicId).map(this::toDomain);
    }

    private TopicEntity toEntity(Topic topic) {
        TopicEntity entity = new TopicEntity(
            topic.forumId(),
            topic.categoryId(),
            topic.title(),
            joinTags(topic.tags()),
            topic.status().name(),
            topic.createdById(),
            topic.createdBy(),
            topic.createdAt(),
            topic.updatedById(),
            topic.updatedBy(),
            topic.updatedAt()
        );
        if (topic.id() != null) {
            entity.setId(topic.id());
            entity.setVersion(topic.version());
        }
        return entity;
    }

    private Topic toDomain(TopicEntity entity) {
        return new Topic(
            entity.getId(),
            entity.getForumId(),
            entity.getCategoryId(),
            entity.getTitle(),
            splitTags(entity.getTags()),
            TopicStatus.valueOf(entity.getStatus()),
            entity.getCreatedById(),
            entity.getCreatedBy(),
            entity.getCreatedAt(),
            entity.getUpdatedById(),
            entity.getUpdatedBy(),
            entity.getUpdatedAt(),
            entity.getVersion()
        );
    }

    private String joinTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "";
        }
        return String.join(",", tags);
    }

    private List<String> splitTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tags.split(","))
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .collect(Collectors.toList());
    }
}
