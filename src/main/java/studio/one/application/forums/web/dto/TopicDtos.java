package studio.one.application.forums.web.dto;

import java.time.OffsetDateTime;
import java.util.List;

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
public class TopicDtos {
    @Getter
    @Setter
    public static class CreateTopicRequest {
        private Long categoryId;
        private String title;
        private List<String> tags;
    }

    @Getter
    @Setter
    public static class ChangeTopicStatusRequest {
        private String status;
    }

    @Getter
    @Setter
    public static class PinTopicRequest {
        private boolean pinned;
    }

    @Getter
    @Setter
    public static class LockTopicRequest {
        private boolean locked;
    }

    @Getter
    @Setter
    public static class TopicResponse {
        private Long id;
        private Long categoryId;
        private String title;
        private List<String> tags;
        private String status;
        private OffsetDateTime updatedAt;
    }

    @Getter
    @Setter
    public static class TopicSummaryResponse {
        private Long id;
        private String title;
        private String status;
        private OffsetDateTime updatedAt;
    }
}
