package plus.jdk.milvus.wrapper;

import io.milvus.common.clientenum.ConsistencyLevelEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.jdk.milvus.model.IIndexExtra;
import plus.jdk.milvus.record.VectorModel;


@Data
@EqualsAndHashCode(callSuper = true)
public class LambdaSearchWrapper<T extends VectorModel<?>> extends AbstractLambdaWrapper<T> {

    /**
     * 额外的索引查询参数
     * Search parameter(s) specific to the index.
     * See <a href="https://milvus.io/docs/index.md">Vector Index</a> for more information.
     */
    private IIndexExtra extra;

    /**
     * 查询最相似的多少条数据
     * Number of the most similar results to return.
     */
    private Integer topK = 10;
}
