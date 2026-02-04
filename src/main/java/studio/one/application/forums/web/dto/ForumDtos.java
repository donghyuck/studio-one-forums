package studio.one.application.forums.web.dto;

import java.time.OffsetDateTime;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Forums 웹 API DTO.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
public class ForumDtos {
    @Getter
    @Setter
    public static class CreateForumRequest {
        private String slug;
        private String name;
        private String description;
        private String type;
        private String viewType;
        private Map<String, String> properties;
    }

    @Getter
    @Setter
    public static class UpdateForumSettingsRequest {
        private String name;
        private String description;
        private String type;
        private String viewType;
        private Map<String, String> properties;
    }

    @Getter
    @Setter
    public static class ForumResponse {
        private Long id;
        private String slug;
        private String name;
        private String description;
        private String type;
        private String viewType;
        private Map<String, String> properties;
        private OffsetDateTime updatedAt;
    }

    @Getter
    @Setter
    public static class ForumSummaryResponse {
        private String slug;
        private String name;
        private String type;
        private String viewType;
        private OffsetDateTime updatedAt;
        private long topicCount;
        private long postCount;
        private OffsetDateTime lastActivityAt;
        private Long lastActivityById;
        private String lastActivityBy;
        private String lastActivityType;
        private Long lastActivityId;
    }
}
