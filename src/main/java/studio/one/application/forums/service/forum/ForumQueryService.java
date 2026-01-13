package studio.one.application.forums.service.forum;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import studio.one.application.forums.domain.exception.ForumNotFoundException;
import studio.one.application.forums.domain.model.Forum;
import studio.one.application.forums.domain.repository.ForumRepository;
import studio.one.application.forums.domain.vo.ForumSlug;
import studio.one.application.forums.service.forum.query.ForumDetailView;
import studio.one.application.forums.service.forum.query.ForumSummaryView;

@Service
public class ForumQueryService {
    private final ForumRepository forumRepository;

    public ForumQueryService(ForumRepository forumRepository) {
        this.forumRepository = forumRepository;
    }

    public ForumDetailView getForum(String slug) {
        Forum forum = forumRepository.findBySlug(ForumSlug.of(slug))
            .orElseThrow(() -> ForumNotFoundException.bySlug(slug));
        return new ForumDetailView(
            forum.id(),
            forum.slug().value(),
            forum.name(),
            forum.description(),
            forum.updatedAt(),
            forum.version()
        );
    }

    public List<ForumSummaryView> listForums() {
        return forumRepository.findAll()
            .stream()
            .map(forum -> new ForumSummaryView(forum.slug().value(), forum.name(), forum.updatedAt()))
            .collect(Collectors.toList());
    }
}
