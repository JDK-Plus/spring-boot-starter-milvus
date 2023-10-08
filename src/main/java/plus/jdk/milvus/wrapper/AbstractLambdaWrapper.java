package plus.jdk.milvus.wrapper;

import io.milvus.common.clientenum.ConsistencyLevelEnum;
import lombok.Getter;
import lombok.Setter;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.common.Operator;
import plus.jdk.milvus.common.SFunction;
import plus.jdk.milvus.common.StringPool;
import plus.jdk.milvus.common.operator.LogicOperatorComputer;
import plus.jdk.milvus.global.MilvusClientService;
import plus.jdk.milvus.model.WrapperModel;
import plus.jdk.milvus.record.VectorModel;
import plus.jdk.milvus.selector.MilvusSelector;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <a href="https://milvus.io/docs/boolean.md">向量相似性检索</a>
 */
@Getter
public abstract class AbstractLambdaWrapper<T extends VectorModel<? extends VectorModel<?>>> implements StringPool, Serializable {

    /**
     * 普通的查询参数
     */
    private final List<WrapperModel<T>> wrapperModels = new ArrayList<>();
    @Setter
    protected Class<T> entityType;
    /**
     * 查询中使用的一致性等级
     */
    @Setter
    private ConsistencyLevelEnum consistencyLevel = ConsistencyLevelEnum.STRONG;
    @Setter
    private List<String> partitionNames = new ArrayList<>();
    @Setter
    @Deprecated
    private Long travelTimestamp;
    @Setter
    @Deprecated
    private Long gracefulTime;
    @Setter
    @Deprecated
    private Long ignoreGrowing;

    protected String getTableColumnName(SFunction<?, ?> column, Class<?> clazz) throws MilvusException {
        MilvusClientService milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.getColumnName(column, clazz);
    }

    public <R> AbstractLambdaWrapper<T> eq(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.eq, value, null));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> le(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.le, value, null));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> lt(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.lt, value, null));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> ge(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.ge, value, null));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> gt(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.gt, value, null));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> ne(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.ne, value, null));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> like(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.like, value, null));
        return this;
    }

    public <R> AbstractLambdaWrapper<T> not_like(SFunction<T, R> column, R value) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.not_like, value, null));
        return this;
    }

    @SafeVarargs
    public final AbstractLambdaWrapper<T> and(AbstractLambdaWrapper<T>... wrappers) {
        this.wrapperModels.add(new WrapperModel<>(null, Operator.and, wrappers, null));
        return this;
    }

    @SafeVarargs
    public final AbstractLambdaWrapper<T> or(AbstractLambdaWrapper<T>... wrappers) {
        this.wrapperModels.add(new WrapperModel<>(null, Operator.or, wrappers, null));
        return this;
    }

    @SafeVarargs
    public final AbstractLambdaWrapper<T> not(AbstractLambdaWrapper<T>... wrappers) {
        this.wrapperModels.add(new WrapperModel<>(null, Operator.not, wrappers, null));
        return this;
    }

    @SafeVarargs
    public final <R> AbstractLambdaWrapper<T> in(SFunction<T, R> column, R... values) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.in, values, null));
        return this;
    }

    /**
     * only support milvus2.3+
     * <pre>{@code
     * LambdaQueryWrapper<UserBlogVector> wrapper = new LambdaQueryWrapper<>();
     * jsonWrapper.contains(UserBlogVector::getBlogType, Arrays.asList("1", "2"), "type");
     * }</pre>
     *
     * @param identifier the {@code Object} to check (maybe {@code null})
     */
    public final <R> AbstractLambdaWrapper<T> contains(SFunction<T, R> column, List<?> values, @Nonnull Object... identifier) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.contains, values, identifier));
        return this;
    }

    /**
     * only support milvus2.3+
     * <pre>{@code
     * LambdaQueryWrapper<UserBlogVector> wrapper = new LambdaQueryWrapper<>();
     * jsonWrapper.contains_all(UserBlogVector::getBlogType, Arrays.asList("1", "2"), "type");
     * }</pre>
     *
     * @param identifier the {@code Object} to check (maybe {@code null})
     */
    public final <R> AbstractLambdaWrapper<T> contains_all(SFunction<T, R> column, List<?> values, @Nonnull Object... identifier) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.contains_all, values, identifier));
        return this;
    }

    /**
     * only support milvus2.3+
     * <pre>{@code
     * LambdaQueryWrapper<UserBlogVector> wrapper = new LambdaQueryWrapper<>();
     * jsonWrapper.contains_any(UserBlogVector::getBlogType, Arrays.asList("1", "2"), "type");
     * }</pre>
     *
     * @param identifier the {@code Object} to check (maybe {@code null})
     */
    public final <R> AbstractLambdaWrapper<T> contains_any(SFunction<T, R> column, List<?> values, @Nonnull Object... identifier) {
        this.wrapperModels.add(new WrapperModel<>(column, Operator.contains_any, values, identifier));
        return this;
    }

    public String buildExpression(Class<?> clazz) throws MilvusException {
        List<String> results = new ArrayList<>();
        for (WrapperModel<T> wrapperModel : wrapperModels) {
            Operator operator = wrapperModel.getOperator();
            String columnName = null;
            if (!(operator.getIOperatorComputer() instanceof LogicOperatorComputer)) {
                columnName = getTableColumnName(wrapperModel.getColumn(), clazz);
            }
            String compute = operator.getIOperatorComputer().compute(columnName, wrapperModel.getValue(), wrapperModel.getIdentifier(), clazz);
            results.add(compute);
        }
        for (int i = 0; i < results.size(); i++) {
            if (i == 0) {
                continue;
            }
            if (!Operator.matchAndOr(results.get(i))) {
                results.set(i, AND + SPACE + results.get(i));
                continue;
            }
            results.set(i, results.get(i));
        }

        return String.join(SPACE, results);
    }
}
