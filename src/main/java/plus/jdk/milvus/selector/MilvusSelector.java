package plus.jdk.milvus.selector;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import plus.jdk.milvus.global.handler.UnknownTypeHandler;

import javax.annotation.Nullable;

@Configuration
public class MilvusSelector implements ApplicationContextAware {

    public static ApplicationContext applicationContext;

    @Bean
    public UnknownTypeHandler defaultEmbeddingTypeHandler() {
        return new UnknownTypeHandler();
    }

    @Override
    public void setApplicationContext(@Nullable ApplicationContext applicationContext) throws BeansException {
        MilvusSelector.applicationContext = applicationContext;
    }
}
