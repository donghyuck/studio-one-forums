package studio.one.application.forums.service.audit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import studio.one.application.forums.persistence.jdbc.ForumAuditLogQueryRepository;
import studio.one.application.forums.persistence.jdbc.ForumAuditLogRow;
import studio.one.application.forums.service.audit.query.ForumAuditLogQuery;
import studio.one.application.forums.service.audit.query.ForumAuditLogView;

/**
 * Forums 감사 로그 조회 서비스.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-02-05  Son Donghyuck  최초 생성
 * </pre>
 */
@Service
public class ForumAuditLogQueryService {
    private final ForumAuditLogQueryRepository repository;
    private final ObjectProvider<ObjectMapper> objectMapperProvider;

    public ForumAuditLogQueryService(ForumAuditLogQueryRepository repository,
                                     ObjectProvider<ObjectMapper> objectMapperProvider) {
        this.repository = repository;
        this.objectMapperProvider = objectMapperProvider;
    }

    public Page<ForumAuditLogView> find(ForumAuditLogQuery query, Pageable pageable) {
        return repository.find(query, pageable)
            .map(this::toView);
    }

    private ForumAuditLogView toView(ForumAuditLogRow row) {
        return new ForumAuditLogView(
            row.getAuditId(),
            row.getForumId(),
            row.getEntityType(),
            row.getEntityId(),
            row.getAction(),
            row.getActorId(),
            row.getAt(),
            parseDetail(row.getDetail())
        );
    }

    private Map<String, Object> parseDetail(String detailPayload) {
        if (detailPayload == null) {
            return null;
        }
        ObjectMapper mapper = objectMapperProvider.getIfAvailable();
        if (mapper == null) {
            return Map.of("raw", detailPayload);
        }
        try {
            return mapper.readValue(detailPayload, new TypeReference<Map<String, Object>>() {});
        } catch (Exception ex) {
            return Map.of("raw", detailPayload);
        }
    }
}
