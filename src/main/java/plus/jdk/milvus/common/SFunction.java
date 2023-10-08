package plus.jdk.milvus.common;

@FunctionalInterface
public interface SFunction<T, R> extends java.util.function.Function<T, R>, java.io.Serializable {
}