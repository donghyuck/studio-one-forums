package studio.one.application.forums.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import studio.one.platform.autoconfigure.FeaturesProperties.FeatureToggle;
import studio.one.platform.autoconfigure.PersistenceProperties;
import studio.one.platform.autoconfigure.WebEndpointProperties;
import studio.one.platform.constant.PropertyKeys;

/**
 * Forums 자동 설정.
 *
 * <p>
 * 개정이력
 * </p>
 * 
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@ConfigurationProperties(prefix = PropertyKeys.Features.PREFIX + ".forums")
@Getter
@Setter
public class ForumsFeatureProperties extends FeatureToggle {

    private WebEndpointProperties web = new WebEndpointProperties();
    private CacheProperties cache = new CacheProperties();
    private AuthzProperties authz = new AuthzProperties();
    private AttachmentProperties attachments = new AttachmentProperties();

    public PersistenceProperties.Type resolvePersistence(PersistenceProperties.Type globalDefault) {
        return super.resolvePersistence(globalDefault);
    }

    @Getter
    @Setter
    public static class CacheProperties {
        private boolean enabled = true;
        private Duration listTtl = Duration.ofSeconds(60);
        private Duration detailTtl = Duration.ofMinutes(5);
        private long listMaxSize = 10_000L;
        private long detailMaxSize = 50_000L;
        private boolean recordStats = true;
    }

    @Getter
    @Setter
    public static class AuthzProperties {
        private List<String> adminRoles = new ArrayList<>(List.of("ROLE_ADMIN", "ADMIN"));
    }

    @Getter
    @Setter
    public static class AttachmentProperties {
        /**
         * ObjectType ID for forum_post attachments (DB objecttype).
         */
        private int objectType = 0;
    }
}
