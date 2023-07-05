package plus.jdk.milvus.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import plus.jdk.milvus.annotation.EnableMilvusPlus;
import plus.jdk.milvus.config.MilvusPlusProperties;

@Slf4j
@Configuration
@EnableMilvusPlus
@ConditionalOnProperty(prefix = "plus.jdk.milvus", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(MilvusPlusProperties.class)
public class MilvusAutoConfiguration {

    public MilvusAutoConfiguration(MilvusPlusProperties milvusPlusProperties) {
        log.info("{}", milvusPlusProperties);
    }
}
