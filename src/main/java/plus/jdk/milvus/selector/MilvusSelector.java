package plus.jdk.milvus.selector;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import plus.jdk.milvus.config.MilvusPlusProperties;
import plus.jdk.milvus.global.DefaultEmbeddingTypeHandler;
import plus.jdk.milvus.global.MilvusClientService;

@Configuration
public class MilvusSelector extends WebApplicationObjectSupport implements BeanFactoryAware, WebMvcConfigurer {

    public static BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        MilvusSelector.beanFactory = beanFactory;
    }

    @Bean
    public DefaultEmbeddingTypeHandler getDefaultEmbeddingTypeHandler() {
        return new DefaultEmbeddingTypeHandler();
    }

    @Bean
    public MilvusClientService getMilvusClientService(MilvusPlusProperties properties) {
        return new MilvusClientService(properties, beanFactory, getApplicationContext());
    }
}
