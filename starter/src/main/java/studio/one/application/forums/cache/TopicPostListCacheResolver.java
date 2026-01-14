package studio.one.application.forums.cache;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Caffeine;

import studio.one.application.forums.constant.CacheNames;

/**
 * Forums 캐시 설정.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Component("topicPostListCacheResolver")
@ConditionalOnBean(CacheManager.class)
public class TopicPostListCacheResolver implements CacheResolver {
    private final CacheManager cacheManager;
    private final Duration listTtl;
    private final boolean cacheEnabled;
    private final long listMaxSize;
    private final boolean recordStats;

    public TopicPostListCacheResolver(CacheManager cacheManager,
                                      @Value("${studio.features.forums.cache.enabled:true}") boolean cacheEnabled,
                                      @Value("${studio.features.forums.cache.list-ttl:60s}") Duration listTtl,
                                      @Value("${studio.features.forums.cache.list-max-size:10000}") long listMaxSize,
                                      @Value("${studio.features.forums.cache.record-stats:true}") boolean recordStats) {
        this.cacheManager = cacheManager;
        this.cacheEnabled = cacheEnabled;
        this.listTtl = listTtl;
        this.listMaxSize = listMaxSize;
        this.recordStats = recordStats;
    }

    @Override
    public Collection<? extends Cache> resolveCaches(CacheOperationInvocationContext<?> context) {
        Object[] args = context.getArgs();
        if (args.length == 0 || args[0] == null) {
            return Collections.emptyList();
        }
        Long topicId = (Long) args[0];
        String cacheName = CacheNames.Post.listCacheName(topicId);
        Cache cache = resolveCache(cacheName);
        return cache == null ? Collections.emptyList() : Collections.singletonList(cache);
    }

    private Cache resolveCache(String cacheName) {
        if (!cacheEnabled) {
            return null;
        }
        if (cacheManager instanceof CaffeineCacheManager) {
            CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;
            if (!caffeineCacheManager.getCacheNames().contains(cacheName)) {
                Caffeine<Object, Object> builder = Caffeine.newBuilder()
                    .expireAfterWrite(listTtl)
                    .maximumSize(listMaxSize);
                if (recordStats) {
                    builder = builder.recordStats();
                }
                caffeineCacheManager.registerCustomCache(cacheName, builder.build());
            }
        }
        return cacheManager.getCache(cacheName);
    }
}
