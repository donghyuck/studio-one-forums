package studio.one.application.forums.domain.event;

/**
 * Forums 도메인 이벤트.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public interface ForumsCacheEvictableEvent {
    String forumSlug();

    Long topicId();
}
