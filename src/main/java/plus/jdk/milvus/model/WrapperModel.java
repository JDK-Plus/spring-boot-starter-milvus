package plus.jdk.milvus.model;

import lombok.Getter;
import plus.jdk.milvus.common.Operator;
import plus.jdk.milvus.common.SFunction;

@Getter
public class WrapperModel<T> {

    /**
     * 根据哪一列来做查询
     */
    private SFunction<T, ?> column;
    /**
     * 操作符
     */
    private Operator operator;
    /**
     * 输入的值
     */
    private Object value;
    /**
     * 如果是列类型为DataType.JSON，那么需要指定要筛选json的key
     * 例如 当column为props 要筛选的键值为ids 那么identifier就是 ["ids"]
     */
    private Object identifier;

    public WrapperModel(SFunction<T, ?> column, Operator operator, Object value, Object identifier) {
        this.column = column;
        this.operator = operator;
        this.value = value;
        this.identifier = identifier;
    }

    public WrapperModel<T> setColumn(SFunction<T, ?> column) {
        this.column = column;
        return this;
    }

    public WrapperModel<T> setOperator(Operator operator) {
        this.operator = operator;
        return this;
    }

    public WrapperModel<T> setValue(Object value) {
        this.value = value;
        return this;
    }

    public WrapperModel<T> setIdentifier(Object identifier) {
        this.identifier = identifier;
        return this;
    }
}
