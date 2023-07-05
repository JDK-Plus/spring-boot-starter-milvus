package plus.jdk.milvus.global;

import java.util.List;

public interface EmbeddingTypeHandler<T, V> {

    /**
     * 计算值的向量
     */
    List<V> computeDataVector(T data);
}
