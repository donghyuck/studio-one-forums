package studio.one.application.forums.web.dto;

import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

public class ForumMemberDtos {
    @Getter
    @Setter
    public static class UpsertMemberRequest {
        private Long userId;
        private String role;
    }

    @Getter
    @Setter
    public static class MemberResponse {
        private Long userId;
        private String role;
        private Long createdById;
        private OffsetDateTime createdAt;
    }
}
