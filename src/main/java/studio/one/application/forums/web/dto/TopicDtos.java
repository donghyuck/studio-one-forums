package studio.one.application.forums.web.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class TopicDtos {
    public static class CreateTopicRequest {
        public Long categoryId;
        public String title;
        public List<String> tags;
    }

    public static class ChangeTopicStatusRequest {
        public String status;
    }

    public static class TopicResponse {
        public Long id;
        public Long categoryId;
        public String title;
        public List<String> tags;
        public String status;
        public OffsetDateTime updatedAt;
    }

    public static class TopicSummaryResponse {
        public Long id;
        public String title;
        public String status;
        public OffsetDateTime updatedAt;
    }
}
