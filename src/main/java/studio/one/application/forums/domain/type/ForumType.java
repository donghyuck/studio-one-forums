package studio.one.application.forums.domain.type;

import java.util.Locale;

public enum ForumType {
    COMMON,
    NOTICE,
    SECRET,
    ADMIN_ONLY;

    public static ForumType from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("forum type is required");
        }
        try {
            return ForumType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid forum type: " + value, ex);
        }
    }
}
