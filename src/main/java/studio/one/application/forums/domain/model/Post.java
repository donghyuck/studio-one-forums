package studio.one.application.forums.domain.model;

import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Forums 도메인 모델.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public class Post {
    private final Long id;
    private final Long topicId;
    private String content;
    private Long createdById;
    private String createdBy;
    private OffsetDateTime createdAt;
    private Long updatedById;
    private String updatedBy;
    private OffsetDateTime updatedAt;

}
