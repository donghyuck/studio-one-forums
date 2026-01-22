package studio.one.application.forums.domain.type;

import java.util.Locale;

public enum ForumViewType {
    GENERAL,
    GALLERY,
    VIDEO,
    LIBRARY,
    NOTICE;

    public static ForumViewType from(String value) {
        if (value == null || value.isBlank()) {
            return GENERAL;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return ForumViewType.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid forum viewType: " + value, ex);
        }
    }
}
