package studio.one.application.forums.web.etag;

import studio.one.application.forums.domain.exception.IfMatchInvalidException;
import studio.one.application.forums.domain.exception.IfMatchRequiredException;

public final class EtagUtil {
    private EtagUtil() {
    }

    public static String buildWeakEtag(long version) {
        return "W/\"" + version + "\"";
    }

    public static long parseIfMatchVersion(String ifMatch) {
        if (ifMatch == null || ifMatch.isBlank()) {
            throw new IfMatchRequiredException();
        }
        String first = ifMatch.split(",")[0].trim();
        if (first.startsWith("W/")) {
            first = first.substring(2).trim();
        }
        if (first.startsWith("\"") && first.endsWith("\"") && first.length() >= 2) {
            first = first.substring(1, first.length() - 1);
        }
        if (first.isBlank() || "*".equals(first)) {
            throw new IfMatchInvalidException(ifMatch);
        }
        try {
            return Long.parseLong(first);
        } catch (NumberFormatException ex) {
            throw new IfMatchInvalidException(ifMatch);
        }
    }
}
