package plus.jdk.milvus.conditions.query;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import plus.jdk.milvus.conditions.AbstractWrapper;
import plus.jdk.milvus.conditions.SharedString;
import plus.jdk.milvus.conditions.segments.MergeSegments;
import plus.jdk.milvus.record.VectorModel;
import plus.jdk.milvus.wrapper.LambdaQueryWrapper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entity 对象封装操作类
 */
public class QueryWrapper<T extends VectorModel<? extends VectorModel<?>>> extends AbstractWrapper<T, String, QueryWrapper<T>>
        implements Query<QueryWrapper<T>, T, String> {

    /**
     * 查询字段
     */
    protected final SharedString exprSelect = new SharedString();
    @Getter
    @Setter
    @Accessors(chain = true)
    private Long offset;
    @Getter
    @Setter
    @Accessors(chain = true)
    private Long limit;

    public QueryWrapper() {
        this((T) null);
    }

    public QueryWrapper(T entity) {
        super.setEntity(entity);
        super.initNeed();
    }

    public QueryWrapper(Class<T> entityClass) {
        super.setEntityClass(entityClass);
        super.initNeed();
    }

    /**
     * 非对外公开的构造方法,只用于生产嵌套 Expr
     *
     * @param entityClass 本不应该需要的
     */
    private QueryWrapper(T entity, Class<T> entityClass, AtomicInteger paramNameSeq, MergeSegments mergeSegments) {
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
     * @return LambdaQueryWrapper
     */
    public LambdaQueryWrapper<T> lambda() {
        return new LambdaQueryWrapper<>(getEntity(), getEntityClass(), exprSelect, paramNameSeq, expression, offset, limit);
    }

    /**
     * 用于生成嵌套 Expr
     * <p>
     * 故 ExprSelect 不向下传递
     * </p>
     */
    @Override
    protected QueryWrapper<T> instance() {
        return new QueryWrapper<>(getEntity(), getEntityClass(), paramNameSeq, new MergeSegments());
    }

    @Override
    public void clear() {
        super.clear();
        exprSelect.toNull();
    }
}
