package studio.one.application.forums.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import studio.one.platform.autoconfigure.FeaturesProperties.FeatureToggle;
import studio.one.platform.autoconfigure.PersistenceProperties;
import studio.one.platform.autoconfigure.SimpleWebProperties;
import studio.one.platform.constant.PropertyKeys;

@ConfigurationProperties(prefix = PropertyKeys.Features.PREFIX + ".forums")
@Getter
@Setter
public class ForumsFeatureProperties extends FeatureToggle {

    private SimpleWebProperties web = new SimpleWebProperties();

    public PersistenceProperties.Type resolvePersistence(PersistenceProperties.Type globalDefault) {
        return super.resolvePersistence(globalDefault);
    }
}
