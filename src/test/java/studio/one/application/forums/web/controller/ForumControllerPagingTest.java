package studio.one.application.forums.web.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import studio.one.application.forums.domain.acl.PermissionAction;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.CategoryRepository;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.repository.PostRepository;
import studio.one.application.forums.domain.repository.TopicRepository;
import studio.one.application.forums.domain.type.ForumType;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.persistence.jdbc.ForumQueryRepository;
import studio.one.application.forums.service.forum.ForumQueryService;
import studio.one.application.forums.service.forum.query.ForumSummaryView;
import studio.one.application.forums.web.authz.ForumAuthz;
class ForumControllerPagingTest {
    @Test
    void fillsPageFromNextCandidatePage() throws Exception {
        Forum forum1 = forum(1L, "one");
        Forum forum2 = forum(2L, "two");
        Forum forum3 = forum(3L, "three");
        Page<Forum> page0 = new PageImpl<>(List.of(forum1, forum2), PageRequest.of(0, 2), 3);
        Page<Forum> page1 = new PageImpl<>(List.of(forum3), PageRequest.of(1, 2), 3);

        ForumQueryService queryService = new StubForumQueryService(List.of(page0, page1));
        ForumAuthz authz = new StubForumAuthz(Set.of(2L, 3L));
        Object controller = new ForumController(queryService, authz);
        Object response = controller.getClass()
            .getMethod("listForums", String.class, String.class, org.springframework.data.domain.Pageable.class)
            .invoke(controller, null, null, PageRequest.of(0, 2));
        Object body = response.getClass().getMethod("getBody").invoke(response);
        Object data = body.getClass().getMethod("getData").invoke(body);
        @SuppressWarnings("unchecked")
        Page<studio.one.application.forums.web.dto.ForumDtos.ForumSummaryResponse> page =
            (Page<studio.one.application.forums.web.dto.ForumDtos.ForumSummaryResponse>) data;
        List<studio.one.application.forums.web.dto.ForumDtos.ForumSummaryResponse> content = page.getContent();
        assertThat(content).hasSize(2);
        assertThat(content.get(0).getSlug()).isEqualTo("two");
        assertThat(content.get(1).getSlug()).isEqualTo("three");
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    private Forum forum(Long id, String slug) {
        return new Forum(
            id,
            ForumSlug.of(slug),
            slug,
            "",
            ForumType.COMMON,
            Map.of(),
            1L,
            "admin",
            OffsetDateTime.now(),
            1L,
            "admin",
            OffsetDateTime.now(),
            0L
        );
    }

    private static class StubForumQueryService extends ForumQueryService {
        private final List<Page<Forum>> pages;

        private StubForumQueryService(List<Page<Forum>> pages) {
            super(new EmptyForumRepository(), new EmptyForumQueryRepository());
            this.pages = pages;
        }

        @Override
        public Page<Forum> listForumCandidatesPage(String query, Set<String> inFields, boolean isAdmin,
                                                   boolean isMember, boolean secretListVisible, Long userId,
                                                   org.springframework.data.domain.Pageable pageable) {
            int page = pageable.getPageNumber();
            if (page < pages.size()) {
                return pages.get(page);
            }
            return Page.empty(pageable);
        }

        @Override
        public List<ForumSummaryView> summarizeForums(List<Forum> forums) {
            return forums.stream()
                .map(forum -> new ForumSummaryView(
                    forum.slug().value(),
                    forum.name(),
                    studio.one.application.forums.domain.type.ForumType.COMMON,
                    studio.one.application.forums.domain.type.ForumViewType.GENERAL,
                    forum.updatedAt(),
                    0L,
                    0L,
                    null,
                    null,
                    null,
                    null,
                    null))
                .toList();
        }
    }

    private static class StubForumAuthz extends ForumAuthz {
        private final Set<Long> allowedIds;

        private StubForumAuthz(Set<Long> allowedIds) {
            super(new EmptyForumRepository(),
                new EmptyCategoryRepository(),
                new EmptyTopicRepository(),
                new EmptyPostRepository(),
                new StubForumAuthorizer(),
                new StubForumAccessResolver(),
                false);
            this.allowedIds = allowedIds;
        }

        @Override
        public ForumListVisibility listVisibility() {
            return new ForumListVisibility(true, true, true, 1L);
        }

        @Override
        public List<Forum> filterForumsByAccess(List<Forum> forums, PermissionAction action) {
            return forums.stream()
                .filter(forum -> allowedIds.contains(forum.id()))
                .toList();
        }
    }

    private static class EmptyForumRepository implements ForumRepository {
        @Override
        public Forum save(Forum forum) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Forum> findById(Long forumId) {
            return Optional.empty();
        }

        @Override
        public Optional<Forum> findBySlug(ForumSlug slug) {
            return Optional.empty();
        }

        @Override
        public boolean existsBySlug(ForumSlug slug) {
            return false;
        }

        @Override
        public List<Forum> findAll() {
            return List.of();
        }

        @Override
        public Page<Forum> search(String query, Set<String> inFields, org.springframework.data.domain.Pageable pageable) {
            return Page.empty(pageable);
        }

        @Override
        public List<Forum> searchCandidates(String query, Set<String> inFields, boolean isAdmin,
                                            boolean isMember, boolean secretListVisible, Long userId) {
            return List.of();
        }

        @Override
        public Page<Forum> searchCandidatesPage(String query, Set<String> inFields, boolean isAdmin,
                                                boolean isMember, boolean secretListVisible, Long userId,
                                                org.springframework.data.domain.Pageable pageable) {
            return Page.empty(pageable);
        }
    }

    private static class EmptyForumQueryRepository implements ForumQueryRepository {
        @Override
        public Map<Long, studio.one.application.forums.persistence.jdbc.ForumSummaryMetricsRow> findForumSummaries(List<Long> forumIds, boolean includeHiddenPosts) {
            return Map.of();
        }
    }

    private static class EmptyCategoryRepository implements CategoryRepository {
        @Override
        public java.util.Optional<studio.one.application.forums.domain.model.Category> findById(Long categoryId) {
            return Optional.empty();
        }

        @Override
        public java.util.List<studio.one.application.forums.domain.model.Category> findByForumId(Long forumId) {
            return List.of();
        }

        @Override
        public studio.one.application.forums.domain.model.Category save(studio.one.application.forums.domain.model.Category category) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteById(Long categoryId) {
            throw new UnsupportedOperationException();
        }
    }

    private static class EmptyTopicRepository implements TopicRepository {
        @Override
        public studio.one.application.forums.domain.model.Topic save(studio.one.application.forums.domain.model.Topic topic) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<studio.one.application.forums.domain.model.Topic> findById(Long topicId) {
            return Optional.empty();
        }
    }

    private static class EmptyPostRepository implements PostRepository {
        @Override
        public studio.one.application.forums.domain.model.Post save(studio.one.application.forums.domain.model.Post post) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<studio.one.application.forums.domain.model.Post> findById(Long postId) {
            return Optional.empty();
        }

        @Override
        public List<studio.one.application.forums.domain.model.Post> findByTopicId(Long topicId) {
            return List.of();
        }
    }

    private static class StubForumAuthorizer extends studio.one.application.forums.service.authz.ForumAuthorizer {
        private StubForumAuthorizer() {
            super(new StubForumAccessResolver(),
                new studio.one.application.forums.service.authz.ForumPolicyEngine(
                    new studio.one.application.forums.service.authz.ForumPolicyRegistry(List.of(
                        new studio.one.application.forums.service.authz.policy.CommonBoardTypePolicy(),
                        new studio.one.application.forums.service.authz.policy.NoticeBoardTypePolicy(),
                        new studio.one.application.forums.service.authz.policy.SecretBoardTypePolicy(false),
                        new studio.one.application.forums.service.authz.policy.AdminOnlyBoardTypePolicy()
                    ))),
                new studio.one.application.forums.service.authz.ForumAclAuthorizer(
                    new studio.one.application.forums.service.authz.ForumAuthorizationService(
                        new EmptyAclRepo(), new EmptyTopicRepository(), new EmptyPostRepository())));
        }
    }

    private static class StubForumAccessResolver extends studio.one.application.forums.service.authz.ForumAccessResolver {
        private StubForumAccessResolver() {
            super(new EmptyForumMemberRepository(), Set.of());
        }
    }

    private static class EmptyForumMemberRepository implements studio.one.application.forums.domain.repository.ForumMemberRepository {
        @Override
        public Optional<studio.one.application.forums.domain.type.ForumMemberRole> findRole(long forumId, long userId) {
            return Optional.empty();
        }

        @Override
        public Set<studio.one.application.forums.domain.type.ForumMemberRole> findRoles(long forumId, long userId) {
            return Set.of();
        }

        @Override
        public Map<Long, Set<studio.one.application.forums.domain.type.ForumMemberRole>> findRolesByUserId(long userId) {
            return Map.of();
        }

        @Override
        public List<studio.one.application.forums.domain.model.ForumMember> listMembers(long forumId, int page, int size) {
            return List.of();
        }

        @Override
        public void upsertMemberRole(long forumId, long userId, studio.one.application.forums.domain.type.ForumMemberRole role, Long actorId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeMember(long forumId, long userId) {
            throw new UnsupportedOperationException();
        }
    }

    private static class EmptyAclRepo implements studio.one.application.forums.domain.repository.ForumAclRuleRepository {
        @Override
        public List<studio.one.application.forums.domain.acl.ForumAclRule> findRules(long forumId, Long categoryId,
                                                                                    PermissionAction action, Set<String> roleNames,
                                                                                    Set<Long> roleIds, Long userId, String username) {
            return List.of();
        }

        @Override
        public List<studio.one.application.forums.domain.acl.ForumAclRule> findRulesBulk(Set<Long> forumIds, Long categoryId,
                                                                                        PermissionAction action, Set<String> roleNames,
                                                                                        Set<Long> roleIds, Long userId, String username) {
            return List.of();
        }
 
        @Override
        public List<studio.one.application.forums.domain.acl.ForumAclRule> findByForumId(long forumId) {
            return List.of();
        }

        @Override
        public Optional<studio.one.application.forums.domain.acl.ForumAclRule> findById(long ruleId) {
            return Optional.empty();
        }

        @Override
        public studio.one.application.forums.domain.acl.ForumAclRule save(studio.one.application.forums.domain.acl.ForumAclRule rule) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(studio.one.application.forums.domain.acl.ForumAclRule rule) {
            // no-op
        }
    }
}
