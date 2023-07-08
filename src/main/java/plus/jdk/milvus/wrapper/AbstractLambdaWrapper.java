package plus.jdk.milvus.wrapper;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.common.Operator;
import plus.jdk.milvus.global.MilvusClientService;
import plus.jdk.milvus.model.WrapperModel;
import plus.jdk.milvus.record.VectorModel;
import plus.jdk.milvus.selector.MilvusSelector;

/**
 * 向量相似性检索
 * <a href="https://milvus.io/docs/boolean.md">...</a>
 */
public class AbstractLambdaWrapper<T extends VectorModel<?>> {

    /**
     * 普通的查询参数
     */
    @Getter
    private final List<WrapperModel<T>> wrapperModels = new ArrayList<>();

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

    /**
     * 查询中使用的一致性等级
     */
    @Getter
    @Setter
    private ConsistencyLevelEnum consistencyLevel = ConsistencyLevelEnum.STRONG;


    protected String getTableColumnName(SFunction<?, ?> column, Class<?> clazz) throws MilvusException {
        MilvusClientService milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.getColumnName(column, clazz);
    }

    public <R> AbstractLambdaWrapper<T> vector(SFunction<T, R> column, R value) {
        this.vectorColumn = column;
        this.vectorValue = (List<?>) value;
        return this;
    }

    public <R> AbstractLambdaWrapper<T> eq(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.eq, value));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> le(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.le, value));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> lt(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.lt, value));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> ge(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.ge, value));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> gt(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.gt, value));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> ne(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.ne, value));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> like(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.like, value));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> not_like(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.not_like, value));
        return this;
    }

    public AbstractLambdaWrapper<T> and() {
        this.wrapperModels.add(new WrapperModel<>(null, Operator.and, null));
        return this;
    }

    public AbstractLambdaWrapper<T> or() {
        this.wrapperModels.add(new WrapperModel<>(null, Operator.or, null));
        return this;
    }

    public AbstractLambdaWrapper<T> lbracket() {
        this.wrapperModels.add(new WrapperModel<>(null, Operator.lbracket, null));
        return this;
    }

    public AbstractLambdaWrapper<T> rbracket() {
        this.wrapperModels.add(new WrapperModel<>(null, Operator.rbracket, null));
        return this;
    }

    public String buildExpression(Class<T> clazz) throws MilvusException {
        List<String> results = new ArrayList<>();
        for(WrapperModel<T> wrapperModel: wrapperModels) {
            Operator operator = wrapperModel.getOperator();
            String columnName = getTableColumnName(wrapperModel.getColumn(), clazz);
            results.add(operator.getIOperatorComputer().compute(columnName, wrapperModel.getValue()));
        }
        return String.join(" ", results);
    }
}
