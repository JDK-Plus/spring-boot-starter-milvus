package plus.jdk.milvus.selector;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import plus.jdk.milvus.config.MilvusPlusProperties;
import plus.jdk.milvus.global.MilvusClientService;
import plus.jdk.milvus.global.TypeHandlerRegistry;
import plus.jdk.milvus.global.handler.UnknownTypeHandler;

@Configuration
public class MilvusSelector extends WebApplicationObjectSupport implements BeanFactoryAware, WebMvcConfigurer {

    public static BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        MilvusSelector.beanFactory = beanFactory;
    }

    @Bean
    public UnknownTypeHandler defaultEmbeddingTypeHandler() {
        return new UnknownTypeHandler();
    }

    @Bean
    public MilvusClientService milvusClientService(MilvusPlusProperties properties) {
        return new MilvusClientService(properties, beanFactory);
    }

    @Bean
    public TypeHandlerRegistry typeHandlerRegistry(MilvusPlusProperties properties) {
        return new TypeHandlerRegistry(properties, beanFactory);
    }
}
