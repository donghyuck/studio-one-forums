package studio.one.application.forums.service.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class ForumAuditLogService {
    private static final String INSERT_SQL = """
        insert into tb_forum_audit_log
            (board_id, entity_type, entity_id, action, actor_id, at, detail)
        values
            (:boardId, :entityType, :entityId, :action, :actorId, :at, :detail)
        """;

    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<ObjectMapper> objectMapperProvider;

    public ForumAuditLogService(ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider,
                                ObjectProvider<ObjectMapper> objectMapperProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.objectMapperProvider = objectMapperProvider;
    }

    public void record(Long boardId, String entityType, Long entityId, String action,
                       Long actorId, Map<String, Object> detail) {
        NamedParameterJdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            return;
        }
        String detailPayload = serializeDetail(detail);
        Runnable insert = () -> jdbcTemplate.update(
            INSERT_SQL,
            new MapSqlParameterSource()
                .addValue("boardId", boardId)
                .addValue("entityType", entityType)
                .addValue("entityId", entityId)
                .addValue("action", action)
                .addValue("actorId", actorId)
                .addValue("at", OffsetDateTime.now())
                .addValue("detail", detailPayload)
        );
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    insert.run();
                }
            });
        } else {
            insert.run();
        }
    }

    private String serializeDetail(Map<String, Object> detail) {
        if (detail == null) {
            return null;
        }
        ObjectMapper mapper = objectMapperProvider.getIfAvailable();
        if (mapper == null) {
            return detail.toString();
        }
        try {
            return mapper.writeValueAsString(detail);
        } catch (JsonProcessingException ex) {
            return detail.toString();
        }
    }
}
