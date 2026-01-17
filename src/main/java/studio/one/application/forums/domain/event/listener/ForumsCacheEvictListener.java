package studio.one.application.forums.domain.event.listener;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import studio.one.application.forums.constant.CacheNames;
import studio.one.application.forums.domain.event.CategoryCreatedEvent;
import studio.one.application.forums.domain.event.CategoryDeletedEvent;
import studio.one.application.forums.domain.event.ForumCreatedEvent;
import studio.one.application.forums.domain.event.ForumUpdatedEvent;
import studio.one.application.forums.domain.event.ForumsCacheEvictableEvent;
import studio.one.application.forums.domain.event.PostCreatedEvent;
import studio.one.application.forums.domain.event.TopicCreatedEvent;
import studio.one.application.forums.domain.event.TopicStatusChangedEvent;

/**
 * Forums 도메인 이벤트 리스너.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@RequiredArgsConstructor
public class ForumsCacheEvictListener {

    private final CacheManager cacheManager;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onForumsEvent(ForumsCacheEvictableEvent event) {
        if (event instanceof ForumCreatedEvent) {
            evictForumList();
            return;
        }
        if (event instanceof ForumUpdatedEvent) {
            evictForumList();
            evictForumBySlug(event.forumSlug());
            return;
        }
        if (event instanceof CategoryCreatedEvent) {
            evictCategoriesByForum(event.forumSlug());
            return;
        }
        if (event instanceof CategoryDeletedEvent) {
            evictCategoriesByForum(event.forumSlug());
            return;
        }
        if (event instanceof TopicCreatedEvent) {
            clearTopicList(event.forumSlug());
            return;
        }
        if (event instanceof TopicStatusChangedEvent) {
            clearTopicList(event.forumSlug());
            evictTopicDetail(event.forumSlug(), event.topicId());
            return;
        }
        if (event instanceof PostCreatedEvent) {
            clearPostList(event.topicId());
        }
    }

    private void evictForumList() {
        Cache cache = cacheManager.getCache(CacheNames.Forum.LIST);
        if (cache != null) {
            cache.evict(SimpleKey.EMPTY);
        }
    }

    private void evictForumBySlug(String forumSlug) {
        if (forumSlug == null) {
            return;
        }
        Cache cache = cacheManager.getCache(CacheNames.Forum.BY_SLUG);
        if (cache != null) {
            cache.evict(forumSlug);
        }
    }

    private void evictCategoriesByForum(String forumSlug) {
        if (forumSlug == null) {
            return;
        }
        Cache cache = cacheManager.getCache(CacheNames.Category.BY_FORUM);
        if (cache != null) {
            cache.evict(forumSlug);
        }
    }

    private void clearTopicList(String forumSlug) {
        if (forumSlug == null || forumSlug.isBlank()) {
            return;
        }
        Cache cache = cacheManager.getCache(CacheNames.Topic.listCacheName(forumSlug));
        if (cache != null) {
            cache.clear();
        }
    }

    private void evictTopicDetail(String forumSlug, Long topicId) {
        if (forumSlug == null || topicId == null) {
            return;
        }
        Cache cache = cacheManager.getCache(CacheNames.Topic.BY_ID);
        if (cache != null) {
            cache.evict(new SimpleKey(forumSlug, topicId));
        }
    }

    private void clearPostList(Long topicId) {
        if (topicId == null) {
            return;
        }
        Cache cache = cacheManager.getCache(CacheNames.Post.listCacheName(topicId));
        if (cache != null) {
            cache.clear();
        }
    }
}
