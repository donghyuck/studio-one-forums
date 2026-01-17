package studio.one.application.forums.domain.acl;

import java.util.Locale;

public enum PermissionAction {
    READ_BOARD,
    READ_TOPIC_LIST,
    READ_TOPIC_CONTENT,
    CREATE_TOPIC,
    REPLY_POST,
    EDIT_TOPIC,
    DELETE_TOPIC,
    EDIT_POST,
    DELETE_POST,
    PIN_TOPIC,
    LOCK_TOPIC,
    HIDE_POST,
    MODERATE,
    MANAGE_BOARD;

    public static PermissionAction from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("action is required (available: " + availableValues() + ")");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        try {
            return PermissionAction.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unknown action '" + value + "' (available: " + availableValues() + ")", ex);
        }
    }

    private static String availableValues() {
        StringBuilder builder = new StringBuilder();
        for (PermissionAction action : PermissionAction.values()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(action.name());
        }
        return builder.toString();
    }
}
