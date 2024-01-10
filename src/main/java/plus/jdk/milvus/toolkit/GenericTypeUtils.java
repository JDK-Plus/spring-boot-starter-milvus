package plus.jdk.milvus.toolkit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import plus.jdk.milvus.toolkit.reflect.IGenericTypeResolver;
import plus.jdk.milvus.toolkit.reflect.SpringReflectionHelper;

/**
 * 泛型类工具（用于隔离Spring的代码）
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GenericTypeUtils {
    private static IGenericTypeResolver genericTypeResolver;

    /**
     * 获取泛型工具助手
     *
     * @param clazz      类
     * @param genericIfc 泛型接口
     * @return 泛型工具助手
     */
    public static Class<?>[] resolveTypeArguments(final Class<?> clazz, final Class<?> genericIfc) {
        if (null == genericTypeResolver) {
            // 直接使用 spring 静态方法，减少对象创建
            return SpringReflectionHelper.resolveTypeArguments(clazz, genericIfc);
        }
        return genericTypeResolver.resolveTypeArguments(clazz, genericIfc);
    }

    /**
     * 设置泛型工具助手。如果不想使用Spring封装，可以使用前替换掉
     *
     * @param genericTypeResolver 通用类型解析器
     */
    public static void setGenericTypeResolver(IGenericTypeResolver genericTypeResolver) {
        GenericTypeUtils.genericTypeResolver = genericTypeResolver;
    }
}
