package studio.one.application.forums.service.post.query;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Forums 조회 모델.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Getter
@AllArgsConstructor
public class PostSummaryView {
    private final Long id;
    private final String content;
    private final Long createdById;
    private final String createdBy;
    private final OffsetDateTime createdAt;
}
