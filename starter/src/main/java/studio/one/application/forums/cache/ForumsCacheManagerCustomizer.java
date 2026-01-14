package studio.one.application.forums.cache;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.caffeine.CaffeineCacheManager;
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
@Component
public class ForumsCacheManagerCustomizer implements CacheManagerCustomizer<CaffeineCacheManager> {
    private final boolean cacheEnabled;
    private final Duration listTtl;
    private final Duration detailTtl;
    private final long listMaxSize;
    private final long detailMaxSize;
    private final boolean recordStats;

    public ForumsCacheManagerCustomizer(
        @Value("${studio.features.forums.cache.enabled:true}") boolean cacheEnabled,
        @Value("${studio.features.forums.cache.list-ttl:60s}") Duration listTtl,
        @Value("${studio.features.forums.cache.detail-ttl:5m}") Duration detailTtl,
        @Value("${studio.features.forums.cache.list-max-size:10000}") long listMaxSize,
        @Value("${studio.features.forums.cache.detail-max-size:50000}") long detailMaxSize,
        @Value("${studio.features.forums.cache.record-stats:true}") boolean recordStats) {
        this.cacheEnabled = cacheEnabled;
        this.listTtl = listTtl;
        this.detailTtl = detailTtl;
        this.listMaxSize = listMaxSize;
        this.detailMaxSize = detailMaxSize;
        this.recordStats = recordStats;
    }

    @Override
    public void customize(CaffeineCacheManager cacheManager) {
        if (!cacheEnabled) {
            return;
        }
        registerCache(cacheManager, CacheNames.Forum.LIST, listTtl, listMaxSize);
        registerCache(cacheManager, CacheNames.Category.BY_FORUM, listTtl, listMaxSize);
        registerCache(cacheManager, CacheNames.Forum.BY_SLUG, detailTtl, detailMaxSize);
        registerCache(cacheManager, CacheNames.Topic.BY_ID, detailTtl, detailMaxSize);
    }

    private void registerCache(CaffeineCacheManager cacheManager, String cacheName, Duration ttl, long maxSize) {
        if (ttl == null || cacheManager.getCacheNames().contains(cacheName)) {
            return;
        }
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
            .expireAfterWrite(ttl)
            .maximumSize(maxSize);
        if (recordStats) {
            builder = builder.recordStats();
        }
        cacheManager.registerCustomCache(cacheName, builder.build());
    }
}
