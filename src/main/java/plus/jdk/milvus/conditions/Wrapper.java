package plus.jdk.milvus.conditions;

import org.apache.commons.collections4.CollectionUtils;
import plus.jdk.milvus.conditions.segments.MergeSegments;
import plus.jdk.milvus.record.VectorModel;

/**
 * 条件构造抽象类
 */
public abstract class Wrapper<T extends VectorModel<? extends VectorModel<?>>> implements IExprSegment {

    /**
     * 实体对象（子类实现）
     *
     * @return 泛型 T
     */
    public abstract T getEntity();

    /**
     * 获取 MergeSegments
     *
     * @return 合并片段
     */
    public abstract MergeSegments getExpression();

    /**
     * 查询条件为空
     *
     * @return 是否为空
     */
    public boolean isEmptyOfWhere() {
        return isEmptyOfNormal();
    }

    /**
     * 查询条件不为空(包含entity)
     *
     * @return 是否不为空
     */
    public boolean isNonEmptyOfWhere() {
        return !isEmptyOfWhere();
    }

    /**
     * 查询条件为空(不包含entity)
     *
     * @return 是否为空
     */
    public boolean isEmptyOfNormal() {
        return CollectionUtils.isEmpty(getExpression().getNormal());
    }

    /**
     * 查询条件为空(不包含entity)
     *
     * @return 是否不为空
     */
    public boolean isNonEmptyOfNormal() {
        return !isEmptyOfNormal();
    }

    /**
     * 获取格式化后的执行Expr
     *
     * @return Expr
     * @since 3.3.1
     */
    public String getTargetExpr() {
        return getExprSegment().replaceAll("#\\{.+?}", "?");
    }

    /**
     * 条件清空
     *
     * @since 3.3.1
     */
    abstract public void clear();
}