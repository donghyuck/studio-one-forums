package studio.one.application.forums.web.authz;

import java.lang.reflect.Method;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Forums 권한 검사 도우미.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Component
public class ForumsAuthz {
    private static final String FEATURE = "features:forums";

    private final Object endpointAuthz;
    private final Method canMethod;

    public ForumsAuthz(@Qualifier("endpointAuthz") Object endpointAuthz) {
        this.endpointAuthz = endpointAuthz;
        this.canMethod = resolveCanMethod(endpointAuthz);
    }

    public boolean canListForums() {
        return can("read");
    }

    public boolean canReadForum(String forumSlug) {
        return can("read");
    }

    public boolean canListCategories(String forumSlug) {
        return can("read");
    }

    public boolean canListTopics(String forumSlug) {
        return can("read");
    }

    public boolean canReadTopic(String forumSlug, Long topicId) {
        return can("read");
    }

    public boolean canListPosts(String forumSlug, Long topicId) {
        return can("read");
    }

    public boolean canCreateForum() {
        return can("write");
    }

    public boolean canUpdateForumSettings(String forumSlug) {
        return can("write");
    }

    public boolean canCreateCategory(String forumSlug) {
        return can("write");
    }

    public boolean canCreateTopic(String forumSlug, Long categoryId) {
        return can("write");
    }

    public boolean canChangeTopicStatus(String forumSlug, Long topicId) {
        return can("write");
    }

    public boolean canCreatePost(String forumSlug, Long topicId) {
        return can("write");
    }

    public boolean canManageForumAcl(String forumSlug) {
        return can("write");
    }

    private boolean can(String action) {
        if (canMethod == null) {
            return false;
        }
        try {
            Object result = canMethod.invoke(endpointAuthz, FEATURE, action);
            return Boolean.TRUE.equals(result);
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }

    private static Method resolveCanMethod(Object endpointAuthz) {
        try {
            return endpointAuthz.getClass().getMethod("can", String.class, String.class);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }
}
