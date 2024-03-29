package plus.jdk.milvus.global;

import java.util.Collection;
import java.util.List;

public interface VectorTypeHandler<T> {

    /**
     * @param data 传入的数据
     * @return 因为milvus要求数据输入必须全是list
     */
    List<T> serialize(T data);


    /**
     * @param data 传入的数据
     * @return 目前没用到，暂时不支持，需要后续新增功能
     */
    T deserialize(Collection<T> data);
}
