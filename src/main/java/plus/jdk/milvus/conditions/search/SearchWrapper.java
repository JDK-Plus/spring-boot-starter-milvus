package plus.jdk.milvus.conditions.search;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import plus.jdk.milvus.conditions.AbstractWrapper;
import plus.jdk.milvus.conditions.SharedString;
import plus.jdk.milvus.conditions.segments.MergeSegments;
import plus.jdk.milvus.model.IIndexExtra;
import plus.jdk.milvus.record.VectorModel;
import plus.jdk.milvus.toolkit.support.SFunction;
import plus.jdk.milvus.wrapper.LambdaSearchWrapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchWrapper<T extends VectorModel<? extends VectorModel<?>>> extends AbstractWrapper<T, String, SearchWrapper<T>>
        implements Search<SearchWrapper<T>, T, String> {
    private static final long serialVersionUID = -1L;
    /**
     * 查询字段
     */
    protected final SharedString exprSelect = new SharedString();
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
    private List<?> vectorValue;

    public SearchWrapper() {
        this((T) null);
    }

    public SearchWrapper(T entity) {
        super.setEntity(entity);
        super.initNeed();
    }

    public SearchWrapper(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
    }

    /**
     * 非对外公开的构造方法,只用于生产嵌套 Expr
     *
     * @param entityClass 本不应该需要的
     */
    private SearchWrapper(T entity, Class<T> entityClass, AtomicInteger paramNameSeq,
                          MergeSegments mergeSegments) {
        super.setEntity(entity);
        super.setEntityClass(entityClass);
        this.paramNameSeq = paramNameSeq;
        this.expression = mergeSegments;
    }

    @Override
    public String getExprSelect() {
        return exprSelect.getStringValue();
    }

    /**
     * 返回一个支持 lambda 函数写法的 wrapper
     *
     * @return LambdaSearchWrapper
     */
    public LambdaSearchWrapper<T> lambda() {
        return new LambdaSearchWrapper<>(getEntity(), getEntityClass(), exprSelect, paramNameSeq,
                expression, extra, topK, vectorColumn, vectorValue);
    }

    /**
     * 用于生成嵌套 Expr
     * <p>
     * 故 ExprSelect 不向下传递
     * </p>
     */
    @Override
    protected SearchWrapper<T> instance() {
        return new SearchWrapper<>(getEntity(), getEntityClass(), paramNameSeq, new MergeSegments());
    }

    @Override
    public void clear() {
        super.clear();
        exprSelect.toNull();
    }
}
