package plus.jdk.milvus.wrapper;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import lombok.Data;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.Setter;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.common.Operator;
import plus.jdk.milvus.global.MilvusClientService;
import plus.jdk.milvus.model.WrapperModel;
import plus.jdk.milvus.record.VectorModel;
import plus.jdk.milvus.selector.MilvusSelector;

/**
 * <a href="https://milvus.io/docs/boolean.md">向量相似性检索</a>
 */
public abstract class AbstractLambdaWrapper<T extends VectorModel<? extends VectorModel<?>>> implements Serializable {

    /**
     * 普通的查询参数
     */
    @Getter
    private final List<WrapperModel<T>> wrapperModels = new ArrayList<>();

    /**
     * 查询中使用的一致性等级
     */
    @Getter
    @Setter
    private ConsistencyLevelEnum consistencyLevel = ConsistencyLevelEnum.STRONG;

    @Getter
    @Setter
    private List<String> partitionNames = new ArrayList<>();

    @Getter
    @Setter
    @Deprecated
    private Long travelTimestamp;

    @Getter
    @Setter
    @Deprecated
    private Long gracefulTime;

    @Getter
    @Setter
    @Deprecated
    private Long ignoreGrowing;

    @Getter
    @Setter
    protected Class<T> entityType;

    protected String getTableColumnName(SFunction<?, ?> column, Class<?> clazz) throws MilvusException {
        MilvusClientService milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.getColumnName(column, clazz);
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

    public AbstractLambdaWrapper<T> and(AbstractLambdaWrapper<T> ...wrappers) {
        this.wrapperModels.add(new WrapperModel<>(null, Operator.and, wrappers));
        return this;
    }

    public AbstractLambdaWrapper<T> or(AbstractLambdaWrapper<T> ...wrappers) {
        this.wrapperModels.add(new WrapperModel<>(null, Operator.or, wrappers));
        return this;
    }

    public AbstractLambdaWrapper<T> not(AbstractLambdaWrapper<T> ...wrappers) {
        this.wrapperModels.add(new WrapperModel<>(null, Operator.not, wrappers));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> in(SFunction<T, R> column, R ...values) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.in, values));
        return this;
    }

    public String buildExpression(Class<?> clazz) throws MilvusException {
        List<String> results = new ArrayList<>();
        for(WrapperModel<T> wrapperModel: wrapperModels) {
            Operator operator = wrapperModel.getOperator();
            String columnName = getTableColumnName(wrapperModel.getColumn(), clazz);
            results.add(operator.getIOperatorComputer().compute(columnName, wrapperModel.getValue(), clazz));
        }
        return String.join(" and ", results);
    }
}
