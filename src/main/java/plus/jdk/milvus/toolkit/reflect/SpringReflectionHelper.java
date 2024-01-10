package plus.jdk.milvus.toolkit.reflect;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.core.GenericTypeResolver;

/**
 * Spring 反射辅助类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SpringReflectionHelper {

    public static Class<?>[] resolveTypeArguments(Class<?> clazz, Class<?> genericIfc) {
        return GenericTypeResolver.resolveTypeArguments(clazz, genericIfc);
    }
}
