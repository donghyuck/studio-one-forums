package studio.one.application.forums.domain.property;

import java.util.Set;

public final class ForumPropertyKeys {
    public static final String VIEW_TYPE = "viewType";
    public static final String MEDIA_ALLOWED_EXT = "media.allowedExt";
    public static final String LIBRARY_MAX_FILE_MB = "library.maxFileMb";
    public static final Set<String> ALLOWED_KEYS = Set.of(
        VIEW_TYPE,
        MEDIA_ALLOWED_EXT,
        LIBRARY_MAX_FILE_MB
    );

    private ForumPropertyKeys() {
    }

    public static boolean isAllowed(String key) {
        return key != null && ALLOWED_KEYS.contains(key);
    }
}
