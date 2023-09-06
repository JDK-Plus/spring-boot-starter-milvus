package plus.jdk.milvus.wrapper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import plus.jdk.milvus.common.SFunction;
import plus.jdk.milvus.model.IIndexExtra;
import plus.jdk.milvus.record.VectorModel;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;


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


    /**
     * 指定要检索的向量列
     */
    @Getter
    private SFunction<T, ?> vectorColumn;

    /**
     * 指定输入向量
     */
    @Getter
    private List<?> vectorValue;

    public <R> AbstractLambdaWrapper<T> vector(SFunction<T, R> column, R value) {
        this.vectorColumn = column;
        this.vectorValue = (List<?>) value;
        return this;
    }
}
