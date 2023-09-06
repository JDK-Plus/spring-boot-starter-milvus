package plus.jdk.milvus.common;

/**
 * 泛型类助手（用于隔离Spring的代码）
 *
 * @author noear
 * @author hubin
 * @since 2021-09-03
 */
public interface IGenericTypeResolver {

    Class<?>[] resolveTypeArguments(final Class<?> clazz, final Class<?> genericIfc);
}
