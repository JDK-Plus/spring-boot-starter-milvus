package plus.jdk.milvus.global;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import plus.jdk.milvus.config.MilvusPlusProperties;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TypeHandlerRegistry {

    private final MilvusPlusProperties properties;

    private final BeanFactory beanFactory;

    private final ApplicationContext applicationContext;

    private Map<Class<?>, VectorTypeHandler<?, ?>> typeHandlerInstanceMap = new ConcurrentHashMap<>();

    public TypeHandlerRegistry(MilvusPlusProperties properties,
                               BeanFactory beanFactory,
                               ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.beanFactory = beanFactory;
        this.properties = properties;
    }


}
