package studio.one.application.forums.web.dto;

import java.time.OffsetDateTime;

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
public class PostDtos {
    @Getter
    @Setter
    public static class CreatePostRequest {
        private String content;
    }

    @Getter
    @Setter
    public static class PostResponse {
        private Long id;
        private String content;
        private Long createdById;
        private String createdBy;
        private OffsetDateTime createdAt;
    }
}
