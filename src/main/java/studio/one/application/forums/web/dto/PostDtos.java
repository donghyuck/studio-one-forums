package studio.one.application.forums.web.dto;

import java.time.OffsetDateTime;

public class PostDtos {
    public static class CreatePostRequest {
        public String content;
    }

    public static class PostResponse {
        public Long id;
        public String content;
        public Long createdById;
        public String createdBy;
        public OffsetDateTime createdAt;
    }
}
