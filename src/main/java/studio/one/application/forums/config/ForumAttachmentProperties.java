package studio.one.application.forums.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import studio.one.platform.constant.PropertyKeys;

@ConfigurationProperties(prefix = PropertyKeys.Features.PREFIX + ".forums.attachments")
@Getter
@Setter
public class ForumAttachmentProperties {

    /**
     * ObjectType ID for forum_post attachments (DB-managed object type).
     */
    private int objectType = 0;
}
