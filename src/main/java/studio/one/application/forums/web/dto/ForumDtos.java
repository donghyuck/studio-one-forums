package studio.one.application.forums.web.dto;

import java.time.OffsetDateTime;

public class ForumDtos {
    public static class CreateForumRequest {
        public String slug;
        public String name;
        public String description;
    }

    public static class UpdateForumSettingsRequest {
        public String name;
        public String description;
    }

    public static class ForumResponse {
        public Long id;
        public String slug;
        public String name;
        public String description;
        public OffsetDateTime updatedAt;
    }

    public static class ForumSummaryResponse {
        public String slug;
        public String name;
        public OffsetDateTime updatedAt;
    }
}
