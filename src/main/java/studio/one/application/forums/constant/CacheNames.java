package studio.one.application.forums.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Forums 상수.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public class CacheNames {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Forum {
        public static final String LIST = "forums.list";
        public static final String BY_SLUG = "forums.bySlug";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Category {
        public static final String BY_FORUM = "forums.categories.byForum";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Topic {
        public static final String LIST = "forums.topics.list";
        public static final String BY_ID = "forums.topics.byId";

        public static String listCacheName(String forumSlug) {
            return LIST + "." + forumSlug;
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Post {
        public static final String LIST = "forums.posts.list";

        public static String listCacheName(Long topicId) {
            return LIST + "." + topicId;
        }
    }
}
