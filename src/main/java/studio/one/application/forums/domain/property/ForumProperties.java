package studio.one.application.forums.domain.property;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import studio.one.application.forums.domain.type.ForumViewType;

public final class ForumProperties {
    private ForumProperties() {
    }

    public static Map<String, String> normalizeForWrite(Map<String, String> properties, ForumViewType viewType) {
        Map<String, String> normalized = new LinkedHashMap<>();
        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String key = entry.getKey();
                if (key == null || key.isBlank() || !ForumPropertyKeys.isAllowed(key)) {
                    continue;
                }
                normalized.put(key, entry.getValue());
            }
        }
        ForumViewType resolved = viewType != null ? viewType : readViewType(normalized);
        normalized.put(ForumPropertyKeys.VIEW_TYPE, resolved.name());
        return normalized;
    }

    public static void validateKnownKeys(Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }
        List<String> unknownKeys = new java.util.ArrayList<>();
        for (String key : properties.keySet()) {
            if (key == null || key.isBlank() || !ForumPropertyKeys.isAllowed(key)) {
                unknownKeys.add(key == null || key.isBlank() ? "(blank)" : key);
            }
        }
        if (!unknownKeys.isEmpty()) {
            throw new IllegalArgumentException("unknown forum properties: " + String.join(", ", unknownKeys));
        }
    }

    public static Map<String, String> filterAllowed(Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return Map.of();
        }
        Map<String, String> filtered = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (ForumPropertyKeys.isAllowed(entry.getKey())) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    public static ForumViewType readViewType(Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return ForumViewType.GENERAL;
        }
        return ForumViewType.from(properties.get(ForumPropertyKeys.VIEW_TYPE));
    }
}
