package studio.one.application.forums.service.forum.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Forums 유스케이스 커맨드.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@Getter
@AllArgsConstructor
@Accessors(fluent = true)
public class CreateForumCommand {
    private final String slug;
    private final String name;
    private final String description;
    private final String type;
    private final String viewType;
    private final java.util.Map<String, String> properties;
    private final Long createdById;
    private final String createdBy;
}
