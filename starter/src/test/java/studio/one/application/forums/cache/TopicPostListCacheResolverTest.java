package studio.one.application.forums.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import studio.one.application.forums.constant.CacheNames;

class TopicPostListCacheResolverTest {

    @Test
    void resolveCaches_usesSecondArgumentAsTopicId() {
        CacheManager cacheManager = mock(CacheManager.class);
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn(CacheNames.Post.listCacheName(123L));
        when(cacheManager.getCache(CacheNames.Post.listCacheName(123L))).thenReturn(cache);
        TopicPostListCacheResolver resolver = new TopicPostListCacheResolver(
            cacheManager,
            true,
            Duration.ofSeconds(60),
            100,
            false
        );

        CacheOperationInvocationContext<?> context = mock(CacheOperationInvocationContext.class);
        when(context.getArgs()).thenReturn(new Object[] { "notice", 123L });

        Collection<? extends Cache> caches = resolver.resolveCaches(context);

        assertThat(caches).hasSize(1);
        assertThat(caches.iterator().next().getName()).isEqualTo(CacheNames.Post.listCacheName(123L));
    }

    @Test
    void resolveCaches_acceptsStringTopicId() {
        CacheManager cacheManager = mock(CacheManager.class);
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn(CacheNames.Post.listCacheName(123L));
        when(cacheManager.getCache(CacheNames.Post.listCacheName(123L))).thenReturn(cache);
        TopicPostListCacheResolver resolver = new TopicPostListCacheResolver(
            cacheManager,
            true,
            Duration.ofSeconds(60),
            100,
            false
        );

        CacheOperationInvocationContext<?> context = mock(CacheOperationInvocationContext.class);
        when(context.getArgs()).thenReturn(new Object[] { "notice", "123" });

        Collection<? extends Cache> caches = resolver.resolveCaches(context);

        assertThat(caches).hasSize(1);
        assertThat(caches.iterator().next().getName()).isEqualTo(CacheNames.Post.listCacheName(123L));
    }

    @Test
    void resolveCaches_returnsEmptyWhenArgsDoNotContainValidTopicId() {
        CacheManager cacheManager = mock(CacheManager.class);
        TopicPostListCacheResolver resolver = new TopicPostListCacheResolver(
            cacheManager,
            true,
            Duration.ofSeconds(60),
            100,
            false
        );

        CacheOperationInvocationContext<?> tooShort = mock(CacheOperationInvocationContext.class);
        when(tooShort.getArgs()).thenReturn(new Object[] { "notice" });

        CacheOperationInvocationContext<?> invalid = mock(CacheOperationInvocationContext.class);
        when(invalid.getArgs()).thenReturn(new Object[] { "notice", "abc" });

        assertThat(resolver.resolveCaches(tooShort)).isEmpty();
        assertThat(resolver.resolveCaches(invalid)).isEmpty();
    }
}
