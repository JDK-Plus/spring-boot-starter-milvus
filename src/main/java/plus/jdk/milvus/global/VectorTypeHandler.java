package plus.jdk.milvus.global;

import java.util.List;

public interface VectorTypeHandler<T, V> {

    /**
     * 因为milvus要求数据输入必须全是list
     */
    List<V> serialize(T data);


    /**
     * 目前没用到，暂时不支持，需要后续新增功能
     */
    T deserialize(V data) ;
}
