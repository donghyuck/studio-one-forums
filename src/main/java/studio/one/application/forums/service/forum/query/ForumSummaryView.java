package studio.one.application.forums.service.forum.query;

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
public class ForumSummaryView {
    private final String slug;
    private final String name;
    private final OffsetDateTime updatedAt;
}
