package plus.jdk.milvus.toolkit.reflect;

/**
 * 泛型类助手（用于隔离Spring的代码）
 */
public interface IGenericTypeResolver {

    Class<?>[] resolveTypeArguments(final Class<?> clazz, final Class<?> genericIfc);
}
