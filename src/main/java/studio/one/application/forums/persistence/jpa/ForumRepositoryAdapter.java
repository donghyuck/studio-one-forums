package studio.one.application.forums.persistence.jpa;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.type.ForumMemberRole;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.persistence.jpa.entity.ForumEntity;
import studio.one.application.forums.persistence.jpa.entity.ForumMemberEntity;
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

    @Override
    public Page<Forum> search(String query, Set<String> inFields, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return forumJpaRepository.findAll(pageable).map(this::toDomain);
        }
        Set<String> fields = normalizeInFields(inFields);
        if (fields.isEmpty()) {
            return forumJpaRepository.findAll(pageable).map(this::toDomain);
        }
        String like = "%" + query.toLowerCase() + "%";
        Specification<ForumEntity> spec = (root, criteria, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (String field : fields) {
                predicates.add(cb.like(cb.lower(root.get(field)), like));
            }
            return cb.or(predicates.toArray(new Predicate[0]));
        };
        return forumJpaRepository.findAll(spec, pageable).map(this::toDomain);
    }

    @Override
    public List<Forum> searchCandidates(String query, Set<String> inFields, boolean isAdmin,
                                        boolean isMember, boolean secretListVisible, Long userId) {
        Set<String> fields = normalizeInFields(inFields);
        boolean hasSearch = query != null && !query.isBlank() && !fields.isEmpty();
        String like = hasSearch ? "%" + query.toLowerCase() + "%" : null;
        Specification<ForumEntity> spec = (root, criteria, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasSearch) {
                List<Predicate> searchPredicates = new ArrayList<>();
                for (String field : fields) {
                    searchPredicates.add(cb.like(cb.lower(root.get(field)), like));
                }
                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }
            if (!isAdmin) {
                Predicate visibility = buildVisibilityPredicate(root, criteria, cb, isMember, secretListVisible, userId);
                predicates.add(visibility);
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
        return forumJpaRepository.findAll(spec)
            .stream()
            .map(this::toDomain)
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public Page<Forum> searchCandidatesPage(String query, Set<String> inFields, boolean isAdmin,
                                            boolean isMember, boolean secretListVisible, Long userId, Pageable pageable) {
        Set<String> fields = normalizeInFields(inFields);
        boolean hasSearch = query != null && !query.isBlank() && !fields.isEmpty();
        String like = hasSearch ? "%" + query.toLowerCase() + "%" : null;
        Specification<ForumEntity> spec = (root, criteria, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (hasSearch) {
                List<Predicate> searchPredicates = new ArrayList<>();
                for (String field : fields) {
                    searchPredicates.add(cb.like(cb.lower(root.get(field)), like));
                }
                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }
            if (!isAdmin) {
                Predicate visibility = buildVisibilityPredicate(root, criteria, cb, isMember, secretListVisible, userId);
                predicates.add(visibility);
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
        return forumJpaRepository.findAll(spec, pageable).map(this::toDomain);
    }

    private Predicate buildVisibilityPredicate(javax.persistence.criteria.Root<ForumEntity> root,
                                               javax.persistence.criteria.CriteriaQuery<?> query,
                                               javax.persistence.criteria.CriteriaBuilder cb,
                                               boolean isMember,
                                               boolean secretListVisible,
                                               Long userId) {
        List<Predicate> visibility = new ArrayList<>();
        visibility.add(root.get("type").in(ForumType.COMMON, ForumType.NOTICE));
        if (isMember) {
            visibility.add(cb.equal(root.get("type"), ForumType.SECRET));
        }
        if (userId != null) {
            Subquery<Long> subquery = query.subquery(Long.class);
            javax.persistence.criteria.Root<ForumMemberEntity> member = subquery.from(ForumMemberEntity.class);
            Predicate matchForum = cb.equal(member.get("id").get("forumId"), root.get("id"));
            Predicate matchUser = cb.equal(member.get("id").get("userId"), userId);
            Predicate matchRole = member.get("role").in(EnumSet.of(
                ForumMemberRole.OWNER, ForumMemberRole.ADMIN, ForumMemberRole.MODERATOR));
            subquery.select(member.get("id").get("forumId"))
                .where(matchForum, matchUser, matchRole);
            Predicate memberAdmin = cb.exists(subquery);
            visibility.add(cb.and(cb.equal(root.get("type"), ForumType.ADMIN_ONLY), memberAdmin));
            if (!isMember) {
                visibility.add(cb.and(cb.equal(root.get("type"), ForumType.SECRET), memberAdmin));
            }
        }
        return cb.or(visibility.toArray(new Predicate[0]));
    }

    private Set<String> normalizeInFields(Set<String> inFields) {
        if (inFields == null || inFields.isEmpty()) {
            return Set.of("slug", "name", "description");
        }
        return inFields.stream()
            .filter(field -> field.equals("slug") || field.equals("name") || field.equals("description"))
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private ForumEntity toEntity(Forum forum) {
        ForumType type = forum.type() != null ? forum.type() : ForumType.COMMON;
        ForumEntity entity = new ForumEntity(
            forum.slug().value(),
            forum.name(),
            forum.description(),
            type,
            forum.properties(),
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
        if (forum.properties() != null) {
            entity.setProperties(forum.properties());
        }
        return entity;
    }

    private Forum toDomain(ForumEntity entity) {
        return new Forum(
            entity.getId(),
            ForumSlug.of(entity.getSlug()),
            entity.getName(),
            entity.getDescription(),
            entity.getType() != null ? entity.getType() : ForumType.COMMON,
            entity.getProperties() != null ? entity.getProperties() : Map.of(),
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
