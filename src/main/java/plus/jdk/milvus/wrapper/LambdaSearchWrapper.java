package plus.jdk.milvus.wrapper;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import plus.jdk.milvus.conditions.AbstractLambdaWrapper;
import plus.jdk.milvus.conditions.SharedString;
import plus.jdk.milvus.conditions.search.Search;
import plus.jdk.milvus.conditions.segments.MergeSegments;
import plus.jdk.milvus.model.IIndexExtra;
import plus.jdk.milvus.record.VectorModel;
import plus.jdk.milvus.toolkit.support.SFunction;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lambda 更新封装
 */
public class LambdaSearchWrapper<T extends VectorModel<? extends VectorModel<?>>> extends AbstractLambdaWrapper<T, LambdaSearchWrapper<T>>
        implements Search<LambdaSearchWrapper<T>, T, SFunction<T, ?>>, Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 额外的索引查询参数
     * Search parameter(s) specific to the index.
     * See <a href="https://milvus.io/docs/index.md">Vector Index</a> for more information.
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private IIndexExtra extra;

    /**
     * 查询最相似的多少条数据
     * Number of the most similar results to return.
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private Integer topK = 10;


    /**
     * 指定要检索的向量列
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private SFunction<T, ?> vectorColumn;

    /**
     * 指定输入向量
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private transient List<?> vectorValue;

    private SharedString exprSelect = new SharedString();

    public LambdaSearchWrapper() {
        this((T) null);
    }

    public LambdaSearchWrapper(T entity) {
        super.setEntity(entity);
        super.initNeed();
    }

    public LambdaSearchWrapper(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
    }

    public LambdaSearchWrapper(T entity, Class<T> entityClass, SharedString exprSelect, AtomicInteger paramNameSeq,
                               MergeSegments mergeSegments, IIndexExtra extra, Integer topK, SFunction<T, ?> vectorColumn, List<?> vectorValue) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.exprSelect = exprSelect;
        this.paramNameSeq = paramNameSeq;
        this.expression = mergeSegments;
        this.extra = extra;
        this.topK = topK;
        this.vectorColumn = vectorColumn;
        this.vectorValue = vectorValue;
    }

    public <R> LambdaSearchWrapper<T> vector(SFunction<T, R> column, R value) {
        this.vectorColumn = column;
        this.vectorValue = (List<?>) value;
        return this;
    }

    @Override
    public String getExprSelect() {
        return exprSelect.getStringValue();
    }

    @Override
    protected LambdaSearchWrapper<T> instance() {
        return new LambdaSearchWrapper<>(getEntity(), getEntityClass(), null, paramNameSeq,
                new MergeSegments(), extra, topK, vectorColumn, vectorValue);
    }

    @Override
    public void clear() {
        super.clear();
        expression.clear();
    }
}
