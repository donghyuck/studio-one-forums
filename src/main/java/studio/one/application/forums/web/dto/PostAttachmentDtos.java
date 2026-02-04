package studio.one.application.forums.web.dto;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * Post attachment DTOs for forums API.
 */
public class PostAttachmentDtos {
    @Getter
    @Setter
    public static class AttachmentResponse {
        private long attachmentId;
        private String name;
        private String contentType;
        private long size;
        private long createdBy;
        private OffsetDateTime createdAt;
        private String downloadUrl;
    }
}
