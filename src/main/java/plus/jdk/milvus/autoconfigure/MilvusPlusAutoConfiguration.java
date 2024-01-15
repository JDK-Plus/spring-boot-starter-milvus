package plus.jdk.milvus.autoconfigure;

import io.milvus.client.MilvusServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import plus.jdk.milvus.annotation.EnableMilvusPlus;
import plus.jdk.milvus.config.GlobalConfig;
import plus.jdk.milvus.factory.MilvusPlusFactoryBean;
import plus.jdk.milvus.global.MilvusClientService;
import plus.jdk.milvus.global.handler.AnnotationHandler;
import plus.jdk.milvus.incrementer.IdentifierGenerator;

import java.util.function.Consumer;

@Slf4j
@EnableMilvusPlus
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "plus.jdk.milvus", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(MilvusPlusProperties.class)
public class MilvusPlusAutoConfiguration implements InitializingBean {

    private final MilvusPlusProperties properties;
    private final ApplicationContext applicationContext;


    public MilvusPlusAutoConfiguration(
            MilvusPlusProperties properties,
            ApplicationContext applicationContext
    ) {
        this.properties = properties;
        this.applicationContext = applicationContext;
        log.debug("{}", properties);
    }

    @Override
    public void afterPropertiesSet() {

    }

    @Bean
    @ConditionalOnMissingBean
    public MilvusClientService milvusClientService() {
        MilvusPlusFactoryBean factoryBean = new MilvusPlusFactoryBean();
        GlobalConfig globalConfig = this.properties.getGlobalConfig();
        this.getBeanThen(AnnotationHandler.class, globalConfig::setAnnotationHandler);
        this.getBeanThen(IdentifierGenerator.class, globalConfig::setIdentifierGenerator);
        factoryBean.setGlobalConfig(globalConfig);
        factoryBean.setProperties(properties);
        MilvusServiceClient client = factoryBean.getObject();
        return new MilvusClientService(client);
    }

    /**
     * 检查spring容器里是否有对应的bean,有则进行消费
     *
     * @param clazz    class
     * @param consumer 消费
     * @param <T>      泛型
     */
    private <T> void getBeanThen(Class<T> clazz, Consumer<T> consumer) {
        if (this.applicationContext.getBeanNamesForType(clazz, false, false).length > 0) {
            consumer.accept(this.applicationContext.getBean(clazz));
        }
    }
}
