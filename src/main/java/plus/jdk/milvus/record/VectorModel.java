package plus.jdk.milvus.record;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Setter
@Getter
public abstract class VectorModel<T extends VectorModel<?>> implements Serializable {

    /**
     * 使用向量模糊搜索时的向量距离, 仅当使用向量相似性查找时会赋值
     */
    private Float distance;
}
