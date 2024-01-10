package plus.jdk.milvus.conditions.segments;

import lombok.EqualsAndHashCode;
import plus.jdk.milvus.conditions.IExprSegment;
import plus.jdk.milvus.toolkit.StringPool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Expr 片段集合 处理的抽象类
 */
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractISegmentList extends ArrayList<IExprSegment> implements IExprSegment, StringPool {

    /**
     * 最后一个值
     */
    IExprSegment lastValue = null;
    /**
     * 刷新 lastValue
     */
    boolean flushLastValue = false;
    /**
     * 结果集缓存
     */
    private String exprSegment = EMPTY;
    /**
     * 是否缓存过结果集
     */
    private boolean cacheExprSegment = true;

    /**
     * 重写方法,做个性化适配
     *
     * @param c 元素集合
     * @return 是否添加成功
     */
    @Override
    public boolean addAll(Collection<? extends IExprSegment> c) {
        List<IExprSegment> list = new ArrayList<>(c);
        boolean goon = transformList(list, list.get(0), list.get(list.size() - 1));
        if (goon) {
            cacheExprSegment = false;
            if (flushLastValue) {
                this.flushLastValue(list);
            }
            return super.addAll(list);
        }
        return false;
    }

    /**
     * 在其中对值进行判断以及更改 list 的内部元素
     *
     * @param list         传入进来的 IExprSegment 集合
     * @param firstSegment IEbnfSegment 集合里第一个值
     * @param lastSegment  IEbnfSegment 集合里最后一个值
     * @return true 是否继续向下执行; false 不再向下执行
     */
    protected abstract boolean transformList(List<IExprSegment> list, IExprSegment firstSegment, IExprSegment lastSegment);

    /**
     * 刷新属性 lastValue
     */
    private void flushLastValue(List<IExprSegment> list) {
        lastValue = list.get(list.size() - 1);
    }

    /**
     * 删除元素里最后一个值</br>
     * 并刷新属性 lastValue
     */
    void removeAndFlushLast() {
        remove(size() - 1);
        flushLastValue(this);
    }

    @Override
    public String getExprSegment() {
        if (cacheExprSegment) {
            return exprSegment;
        }
        cacheExprSegment = true;
        exprSegment = childrenExprSegment();
        return exprSegment;
    }

    /**
     * 只有该类进行过 addAll 操作,才会触发这个方法
     * <p>
     * 方法内可以放心进行操作
     *
     * @return ebnfSegment
     */
    protected abstract String childrenExprSegment();

    @Override
    public void clear() {
        super.clear();
        lastValue = null;
        exprSegment = EMPTY;
        cacheExprSegment = true;
    }
}
