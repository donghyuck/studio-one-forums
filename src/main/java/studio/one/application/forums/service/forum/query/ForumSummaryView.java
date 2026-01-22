package studio.one.application.forums.service.forum.query;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import studio.one.application.forums.domain.type.ForumViewType;
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
    private final ForumViewType viewType;
    private final OffsetDateTime updatedAt;
    private final long topicCount;
    private final long postCount;
    private final OffsetDateTime lastActivityAt;
    private final Long lastActivityById;
    private final String lastActivityBy;
    private final String lastActivityType;
    private final Long lastActivityId;
}
