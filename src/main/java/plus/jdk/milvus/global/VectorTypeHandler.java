package plus.jdk.milvus.global;

import java.util.List;

public interface VectorTypeHandler<T, V> {

    /**
     * 计算值的向量
     */
    List<V> serialize(T data);


    T deserialize(List<V> data) ;
}
