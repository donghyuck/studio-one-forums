package studio.one.application.forums.autoconfigure;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import studio.one.application.forums.autoconfigure.condition.ConditionalOnForumsPersistence;
import studio.one.application.forums.persistence.jdbc.CategoryJdbcRepositoryAdapter;
import studio.one.application.forums.persistence.jdbc.ForumMemberJdbcRepositoryAdapter;
import studio.one.application.forums.persistence.jdbc.ForumAclRuleJdbcRepositoryAdapter;
import studio.one.application.forums.persistence.jdbc.ForumJdbcRepositoryAdapter;
import studio.one.application.forums.persistence.jdbc.PostJdbcRepositoryAdapter;
import studio.one.application.forums.persistence.jdbc.PostQueryRepositoryImpl;
import studio.one.application.forums.persistence.jdbc.TopicJdbcRepositoryAdapter;
import studio.one.application.forums.persistence.jdbc.TopicQueryRepositoryImpl;
import studio.one.application.forums.persistence.jpa.CategoryRepositoryAdapter;
import studio.one.application.forums.persistence.jpa.ForumMemberRepositoryAdapter;
import studio.one.application.forums.persistence.jpa.ForumAclRuleRepositoryAdapter;
import studio.one.application.forums.persistence.jpa.ForumRepositoryAdapter;
import studio.one.application.forums.persistence.jpa.PostRepositoryAdapter;
import studio.one.application.forums.persistence.jpa.TopicRepositoryAdapter;
import studio.one.application.forums.persistence.jpa.entity.ForumEntity;
import studio.one.application.forums.persistence.jpa.repo.CategoryJpaRepository;
import studio.one.application.forums.persistence.jpa.repo.ForumMemberJpaRepository;
import studio.one.application.forums.persistence.jpa.repo.ForumAclRuleJpaRepository;
import studio.one.application.forums.persistence.jpa.repo.ForumJpaRepository;
import studio.one.application.forums.persistence.jpa.repo.PostJpaRepository;
import studio.one.application.forums.persistence.jpa.repo.TopicJpaRepository;
import studio.one.application.forums.web.controller.CategoryMgmtController;
import studio.one.application.forums.web.controller.CategoryController;
import studio.one.application.forums.web.controller.ForumMgmtController;
import studio.one.application.forums.web.controller.ForumController;
import studio.one.application.forums.web.controller.PostMgmtController;
import studio.one.application.forums.web.controller.PostController;
import studio.one.application.forums.web.controller.TopicMgmtController;
import studio.one.application.forums.web.controller.TopicController;
import studio.one.application.forums.web.controller.ForumMemberMgmtController;
import studio.one.application.forums.domain.event.listener.ForumsCacheEvictListener;
import studio.one.platform.autoconfigure.EntityScanRegistrarSupport;
import studio.one.platform.autoconfigure.I18nKeys;
import studio.one.platform.autoconfigure.PersistenceProperties;
import studio.one.platform.component.State;
import studio.one.platform.constant.PropertyKeys;
import studio.one.platform.constant.ServiceNames;
import studio.one.platform.service.I18n;
import studio.one.platform.util.I18nUtils;
import studio.one.platform.util.LogUtils;

@AutoConfiguration
@EnableConfigurationProperties(ForumsFeatureProperties.class)
@ComponentScan(
        basePackages = "studio.one.application.forums",
        excludeFilters = {
            @Filter(type = FilterType.ANNOTATION, classes = RestController.class),
            @Filter(type = FilterType.ANNOTATION, classes = Controller.class),
            @Filter(type = FilterType.ANNOTATION, classes = RestControllerAdvice.class),
            @Filter(type = FilterType.ANNOTATION, classes = ControllerAdvice.class),
            @Filter(type = FilterType.REGEX, pattern = "studio\\.one\\.application\\.forums\\.persistence\\..*")
        })
/**
 * Forums 자동 설정.
 *
 * <p>개정이력</p>
 * <pre>
 * 2026-01-14  Son Donghyuck  최초 생성
 * </pre>
 */
@ConditionalOnProperty(prefix = PropertyKeys.Features.PREFIX + ".forums", name = "enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class ForumsAutoConfiguration {

    protected static final String FEATURE_NAME = "Forums";

    @Configuration
    @AutoConfigureBefore(HibernateJpaAutoConfiguration.class)
    @ConditionalOnForumsPersistence(PersistenceProperties.Type.jpa)
    static class JpaEntityScanConfig {
        @Bean
        static BeanDefinitionRegistryPostProcessor entityScanRegistrar(Environment env, ObjectProvider<I18n> i18nProvider) {
            I18n i18n = I18nUtils.resolve(i18nProvider);
            String entityKey = PropertyKeys.Features.PREFIX + ".forums.entity-packages";
            String packageName = ForumEntity.class.getPackage().getName();
            log.info(LogUtils.format(i18n, I18nKeys.AutoConfig.Feature.EntityScan.PREPARING, FEATURE_NAME, entityKey, packageName));
            return EntityScanRegistrarSupport.entityScanRegistrar(entityKey, packageName);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(EntityManagerFactory.class)
    @ConditionalOnForumsPersistence(PersistenceProperties.Type.jpa)
    @EnableJpaRepositories(basePackageClasses = { ForumJpaRepository.class, ForumMemberJpaRepository.class })
    static class JpaWiring {
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnForumsPersistence(PersistenceProperties.Type.jpa)
    static class ForumsJpaRepositoryConfig {
        @Bean
        @ConditionalOnMissingBean(ForumRepositoryAdapter.class)
        ForumRepositoryAdapter forumRepositoryAdapter(ForumJpaRepository repo) {
            return new ForumRepositoryAdapter(repo);
        }

        @Bean
        @ConditionalOnMissingBean(CategoryRepositoryAdapter.class)
        CategoryRepositoryAdapter categoryRepositoryAdapter(CategoryJpaRepository repo) {
            return new CategoryRepositoryAdapter(repo);
        }

        @Bean
        @ConditionalOnMissingBean(TopicRepositoryAdapter.class)
        TopicRepositoryAdapter topicRepositoryAdapter(TopicJpaRepository repo) {
            return new TopicRepositoryAdapter(repo);
        }

        @Bean
        @ConditionalOnMissingBean(PostRepositoryAdapter.class)
        PostRepositoryAdapter postRepositoryAdapter(PostJpaRepository repo) {
            return new PostRepositoryAdapter(repo);
        }

        @Bean
        @ConditionalOnMissingBean(ForumAclRuleRepositoryAdapter.class)
        ForumAclRuleRepositoryAdapter forumAclRuleRepositoryAdapter(ForumAclRuleJpaRepository repo) {
            return new ForumAclRuleRepositoryAdapter(repo);
        }

        @Bean
        @ConditionalOnMissingBean(ForumMemberRepositoryAdapter.class)
        ForumMemberRepositoryAdapter forumMemberRepositoryAdapter(ForumMemberJpaRepository repo) {
            return new ForumMemberRepositoryAdapter(repo);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnForumsPersistence(PersistenceProperties.Type.jdbc)
    static class ForumsJdbcRepositoryConfig {
        @Bean
        @ConditionalOnMissingBean(ForumJdbcRepositoryAdapter.class)
        ForumJdbcRepositoryAdapter forumJdbcRepositoryAdapter(
            @Qualifier(ServiceNames.NAMED_JDBC_TEMPLATE) NamedParameterJdbcTemplate template) {
            return new ForumJdbcRepositoryAdapter(template);
        }

        @Bean
        @ConditionalOnMissingBean(CategoryJdbcRepositoryAdapter.class)
        CategoryJdbcRepositoryAdapter categoryJdbcRepositoryAdapter(
            @Qualifier(ServiceNames.NAMED_JDBC_TEMPLATE) NamedParameterJdbcTemplate template) {
            return new CategoryJdbcRepositoryAdapter(template);
        }

        @Bean
        @ConditionalOnMissingBean(TopicJdbcRepositoryAdapter.class)
        TopicJdbcRepositoryAdapter topicJdbcRepositoryAdapter(
            @Qualifier(ServiceNames.NAMED_JDBC_TEMPLATE) NamedParameterJdbcTemplate template) {
            return new TopicJdbcRepositoryAdapter(template);
        }

        @Bean
        @ConditionalOnMissingBean(PostJdbcRepositoryAdapter.class)
        PostJdbcRepositoryAdapter postJdbcRepositoryAdapter(
            @Qualifier(ServiceNames.NAMED_JDBC_TEMPLATE) NamedParameterJdbcTemplate template) {
            return new PostJdbcRepositoryAdapter(template);
        }

        @Bean
        @ConditionalOnMissingBean(ForumAclRuleJdbcRepositoryAdapter.class)
        ForumAclRuleJdbcRepositoryAdapter forumAclRuleJdbcRepositoryAdapter(
            @Qualifier(ServiceNames.NAMED_JDBC_TEMPLATE) NamedParameterJdbcTemplate template) {
            return new ForumAclRuleJdbcRepositoryAdapter(template);
        }

        @Bean
        @ConditionalOnMissingBean(ForumMemberJdbcRepositoryAdapter.class)
        ForumMemberJdbcRepositoryAdapter forumMemberJdbcRepositoryAdapter(
            @Qualifier(ServiceNames.NAMED_JDBC_TEMPLATE) NamedParameterJdbcTemplate template) {
            return new ForumMemberJdbcRepositoryAdapter(template);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class ForumsJdbcQueryConfig {
        @Bean
        @ConditionalOnMissingBean(TopicQueryRepositoryImpl.class)
        TopicQueryRepositoryImpl topicQueryRepositoryImpl(
            @Qualifier(ServiceNames.NAMED_JDBC_TEMPLATE) NamedParameterJdbcTemplate template,
            ObjectProvider<I18n> i18nProvider) {
            I18n i18n = I18nUtils.resolve(i18nProvider);
            log.info(LogUtils.format(i18n, I18nKeys.AutoConfig.Feature.Service.DETAILS, FEATURE_NAME,
                LogUtils.blue(TopicQueryRepositoryImpl.class, true),
                LogUtils.red(State.CREATED.toString())));
            return new TopicQueryRepositoryImpl(template);
        }

        @Bean
        @ConditionalOnMissingBean(PostQueryRepositoryImpl.class)
        PostQueryRepositoryImpl postQueryRepositoryImpl(
            @Qualifier(ServiceNames.NAMED_JDBC_TEMPLATE) NamedParameterJdbcTemplate template,
            ObjectProvider<I18n> i18nProvider) {
            I18n i18n = I18nUtils.resolve(i18nProvider);
            log.info(LogUtils.format(i18n, I18nKeys.AutoConfig.Feature.Service.DETAILS, FEATURE_NAME,
                LogUtils.blue(PostQueryRepositoryImpl.class, true),
                LogUtils.red(State.CREATED.toString())));
            return new PostQueryRepositoryImpl(template);
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = PropertyKeys.Features.PREFIX + ".forums.web", name = "enabled", havingValue = "true", matchIfMissing = true)
    @Import({
        ForumController.class,
        CategoryController.class,
        TopicController.class,
        PostController.class,
        ForumMgmtController.class,
        ForumMemberMgmtController.class,
        CategoryMgmtController.class,
        TopicMgmtController.class,
        PostMgmtController.class
    })
    static class ForumsWebConfig {

        @Bean
        @ConditionalOnBean(CacheManager.class)
        @ConditionalOnProperty(prefix = PropertyKeys.Features.PREFIX + ".forums.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
        ForumsCacheEvictListener forumsCacheEvictListener(ObjectProvider<CacheManager> cacheManagerProvider,
                                                          ObjectProvider<I18n> i18nProvider) {
            I18n i18n = I18nUtils.resolve(i18nProvider);
            log.info(LogUtils.format(i18n, I18nKeys.AutoConfig.Feature.Service.DETAILS, FEATURE_NAME,
                LogUtils.blue(ForumsCacheEvictListener.class, true),
                LogUtils.red(State.CREATED.toString())));
            return new ForumsCacheEvictListener(cacheManagerProvider.getIfAvailable());
        }
    }
}
