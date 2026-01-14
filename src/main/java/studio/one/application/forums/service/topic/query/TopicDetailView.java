package studio.one.application.forums.service.topic.query;

import java.time.OffsetDateTime;
import java.util.List;

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
public class TopicDetailView {
    private final Long id;
    private final Long categoryId;
    private final String title;
    private final List<String> tags;
    private final String status;
    private final OffsetDateTime updatedAt;
    private final long version;
}
