package plus.jdk.milvus.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import plus.jdk.milvus.incrementer.DefaultIdentifierGenerator;
import plus.jdk.milvus.incrementer.IdentifierGenerator;

@Configuration(proxyBeanMethods = false)
public class IdentifierGeneratorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdentifierGenerator identifierGenerator() {
        return DefaultIdentifierGenerator.getInstance();
    }
}