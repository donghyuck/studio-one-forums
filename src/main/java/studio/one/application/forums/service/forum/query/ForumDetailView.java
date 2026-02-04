package studio.one.application.forums.service.forum.query;

import java.time.OffsetDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import studio.one.application.forums.domain.type.ForumType;
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
public class ForumDetailView {
    private final Long id;
    private final String slug;
    private final String name;
    private final String description;
    private final ForumType type;
    private final ForumViewType viewType;
    private final Map<String, String> properties;
    private final OffsetDateTime updatedAt;
    private final long version;
}
