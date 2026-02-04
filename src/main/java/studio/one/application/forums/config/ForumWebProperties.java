package studio.one.application.forums.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ForumWebProperties {

    private final String basePath;

    public ForumWebProperties(@Value("${studio.features.forums.web.base-path:/api/forums}") String basePath) {
        this.basePath = normalize(basePath);
    }

    public String getBasePath() {
        return basePath;
    }

    private String normalize(String path) {
        if (path == null || path.isBlank()) {
            path = "/api/forums";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }
}
