package plus.jdk.milvus.conditions;

import io.milvus.common.clientenum.ConsistencyLevelEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.SerializationUtils;
import plus.jdk.milvus.conditions.interfaces.Compare;
import plus.jdk.milvus.conditions.interfaces.Func;
import plus.jdk.milvus.conditions.interfaces.Join;
import plus.jdk.milvus.conditions.interfaces.Nested;
import plus.jdk.milvus.conditions.segments.ColumnSegment;
import plus.jdk.milvus.conditions.segments.MergeSegments;
import plus.jdk.milvus.enums.ExprKeyword;
import plus.jdk.milvus.enums.ExprLike;
import plus.jdk.milvus.record.VectorModel;
import plus.jdk.milvus.toolKit.CollectionUtils;
import plus.jdk.milvus.toolKit.Constants;
import plus.jdk.milvus.toolKit.StringUtils;
import plus.jdk.milvus.toolKit.expr.ExprUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static java.util.stream.Collectors.joining;
import static plus.jdk.milvus.enums.ExprKeyword.*;
import static plus.jdk.milvus.enums.WrapperKeyword.APPLY;
import static plus.jdk.milvus.toolKit.StringPool.*;

/**
 * 查询条件封装
 */
@SuppressWarnings({"unchecked"})
public abstract class AbstractWrapper<T extends VectorModel<? extends VectorModel<?>>, R, Children extends AbstractWrapper<T, R, Children>> extends Wrapper<T>
        implements Compare<Children, R>, Nested<Children, Children>, Join<Children>, Func<Children, R> {

    /**
     * 占位符
     */
    protected final Children typedThis = (Children) this;
    /**
     * 必要度量
     */
    protected AtomicInteger paramNameSeq;
    protected MergeSegments expression;
    /**
     * 数据库表映射实体类
     */
    private T entity;
    /**
     * 实体类型(主要用于确定泛型以及取TableInfo缓存)
     */
    private Class<T> entityClass;

    /**
     * 查询中使用的一致性等级
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    private ConsistencyLevelEnum consistencyLevel = ConsistencyLevelEnum.STRONG;

    @Getter
    @Setter
    @Accessors(chain = true)
    private List<String> partitionNames = new ArrayList<>();

    @Override
    public T getEntity() {
        return entity;
    }

    public Children setEntity(T entity) {
        this.entity = entity;
        return typedThis;
    }

    public Class<T> getEntityClass() {
        if (entityClass == null && entity != null) {
            entityClass = (Class<T>) entity.getClass();
        }
        return entityClass;
    }

    public Children setEntityClass(Class<T> entityClass) {
        if (entityClass != null) {
            this.entityClass = entityClass;
        }
        return typedThis;
    }

    @Override
    public Children eq(boolean condition, R column, Object val) {
        return addCondition(condition, column, EQ, val);
    }

    @Override
    public Children ne(boolean condition, R column, Object val) {
        return addCondition(condition, column, NE, val);
    }

    @Override
    public Children gt(boolean condition, R column, Object val) {
        return addCondition(condition, column, GT, val);
    }

    @Override
    public Children ge(boolean condition, R column, Object val) {
        return addCondition(condition, column, GE, val);
    }

    @Override
    public Children lt(boolean condition, R column, Object val) {
        return addCondition(condition, column, LT, val);
    }

    @Override
    public Children le(boolean condition, R column, Object val) {
        return addCondition(condition, column, LE, val);
    }

    @Override
    public Children likeRight(boolean condition, R column, Object val) {
        return likeValue(condition, LIKE, column, val, ExprLike.RIGHT);
    }

    @Override
    public Children notLikeRight(boolean condition, R column, Object val) {
        return likeValue(condition, NOT_LIKE, column, val, ExprLike.RIGHT);
    }

    @Override
    public Children and(boolean condition, Consumer<Children> consumer) {
        return and(condition).addNestedCondition(condition, consumer);
    }

    @Override
    public Children or(boolean condition, Consumer<Children> consumer) {
        return or(condition).addNestedCondition(condition, consumer);
    }

    @Override
    public Children not(boolean condition, Consumer<Children> consumer) {
        return not(condition).addNestedCondition(condition, consumer);
    }

    @Override
    public Children or(boolean condition) {
        return maybeDo(condition, () -> appendExprSegments(OR));
    }

    @Override
    public Children apply(boolean condition, String applyExpr, Object... values) {
        return maybeDo(condition, () -> appendExprSegments(APPLY, () -> formatExprMaybeWithParam(applyExpr, values)));
    }

    @Override
    public Children in(boolean condition, R column, Collection<?> coll) {
        return maybeDo(condition, () -> appendExprSegments(columnToExprSegment(column), IN, inExpression(coll)));
    }

    @Override
    public Children in(boolean condition, R column, Object... values) {
        return maybeDo(condition, () -> appendExprSegments(columnToExprSegment(column), IN, inExpression(values)));
    }

    @Override
    public Children notIn(boolean condition, R column, Collection<?> coll) {
        return maybeDo(condition, () -> appendExprSegments(columnToExprSegment(column), NOT_IN, inExpression(coll)));
    }

    @Override
    public Children notIn(boolean condition, R column, Object... values) {
        return maybeDo(condition, () -> appendExprSegments(columnToExprSegment(column), NOT_IN, inExpression(values)));
    }

    @Override
    public Children func(boolean condition, Consumer<Children> consumer) {
        return maybeDo(condition, () -> consumer.accept(typedThis));
    }

    @Override
    public Children jsonContains(boolean condition, R column, Object value, Object... identifier) {
        return maybeDo(condition, () -> appendExprSegments(JSON, jsonExpression(columnToString(column), value, identifier)));
    }

    @Override
    public Children jsonContainsAll(boolean condition, R column, Collection<?> coll, Object... identifier) {
        return maybeDo(condition, () -> appendExprSegments(JSON_ALL, jsonExpression(columnToString(column), coll, identifier)));
    }

    @Override
    public Children jsonContainsAny(boolean condition, R column, Collection<?> coll, Object... identifier) {
        return maybeDo(condition, () -> appendExprSegments(JSON_ANY, jsonExpression(columnToString(column), coll, identifier)));
    }

    @Override
    public Children arrayContains(boolean condition, R column, Object value) {
        return maybeDo(condition, () -> appendExprSegments(ARRAY, arrayExpression(columnToString(column), value)));
    }

    @Override
    public Children arrayContainsAll(boolean condition, R column, Collection<?> coll) {
        return maybeDo(condition, () -> appendExprSegments(ARRAY_ALL, arrayExpression(columnToString(column), coll)));
    }

    @Override
    public Children arrayContainsAll(boolean condition, R column, Object... values) {
        return maybeDo(condition, () -> appendExprSegments(ARRAY_ALL, arrayExpression(columnToString(column), values)));
    }

    @Override
    public Children arrayContainsAny(boolean condition, R column, Collection<?> coll) {
        return maybeDo(condition, () -> appendExprSegments(ARRAY_ANY, arrayExpression(columnToString(column), coll)));
    }

    @Override
    public Children arrayContainsAny(boolean condition, R column, Object... values) {
        return maybeDo(condition, () -> appendExprSegments(ARRAY_ANY, arrayExpression(columnToString(column), values)));
    }

    @Override
    public Children arrayLength(boolean condition, R column, Number value) {
        return maybeDo(condition, () -> appendExprSegments(ARRAY_LENGTH, columnToExprSegment(column), value::toString));
    }

    /**
     * 内部自用
     * <p>NOT 关键词</p>
     *
     * @param condition 条件
     * @return wrapper
     */
    protected Children not(boolean condition) {
        return maybeDo(condition, () -> appendExprSegments(NOT));
    }

    /**
     * 内部自用
     * <p>拼接 AND</p>
     *
     * @param condition 条件
     * @return wrapper
     */
    protected Children and(boolean condition) {
        return maybeDo(condition, () -> appendExprSegments(ExprKeyword.AND));
    }

    /**
     * 内部自用
     * <p>拼接 LIKE 以及 值</p>
     *
     * @param condition 条件
     * @param column    属性
     * @param keyword   关键字
     * @param exprLike  Expr 关键词
     * @param val       条件值
     * @return wrapper
     */
    protected Children likeValue(boolean condition, ExprKeyword keyword, R column, Object val, ExprLike exprLike) {
        return maybeDo(condition, () -> appendExprSegments(columnToExprSegment(column), keyword,
                () -> ExprUtils.concatLike(val, exprLike)));
    }

    /**
     * 普通查询条件
     *
     * @param condition   是否执行
     * @param column      属性
     * @param exprKeyword Expr 关键词
     * @param val         条件值
     * @return wrapper
     */
    protected Children addCondition(boolean condition, R column, ExprKeyword exprKeyword, Object val) {
        return maybeDo(condition, () -> appendExprSegments(columnToExprSegment(column), exprKeyword,
                () -> formatParam(val)));
    }

    /**
     * 多重嵌套查询条件
     *
     * @param condition 查询条件值
     * @param consumer  消费者
     * @return wrapper
     */
    protected Children addNestedCondition(boolean condition, Consumer<Children> consumer) {
        return maybeDo(condition, () -> {
            final Children instance = instance();
            consumer.accept(instance);
            appendExprSegments(APPLY, instance);
        });
    }

    /**
     * 子类返回一个自己的新对象
     *
     * @return wrapper
     */
    protected abstract Children instance();

    /**
     * 格式化 Expr
     * <p>
     * 支持 "{0}" 这种,或者 "Expr {0} Expr" 这种
     *
     * @param exprStr 可能是Expr片段
     * @param params  参数
     * @return Expr片段
     */
    @SuppressWarnings("SameParameterValue")
    protected final String formatExprMaybeWithParam(String exprStr, Object... params) {
        if (StringUtils.isBlank(exprStr)) {
            return null;
        }
        if (ArrayUtils.isNotEmpty(params)) {
            for (int i = 0; i < params.length; ++i) {
                String target = Constants.LEFT_BRACE + i + Constants.RIGHT_BRACE;
                if (exprStr.contains(target)) {
                    exprStr = exprStr.replace(target, (String) params[i]);
                } else {
                    break;
                }
            }
        }
        return exprStr;
    }

    /**
     * 处理入参
     *
     * @param param 参数
     * @return value
     */
    protected final String formatParam(Object param) {
        if (param instanceof String) {
            return Constants.SINGLE_QUOTE + param + Constants.SINGLE_QUOTE;
        }
        if (param instanceof Number) {
            return param.toString();
        }
        throw new IllegalArgumentException("参数只能是 String 或者 Number 类型");
    }

    /**
     * 函数化的做事
     *
     * @param condition 做不做
     * @param something 做什么
     * @return Children
     */
    protected final Children maybeDo(boolean condition, DoSomething something) {
        if (condition) {
            something.doIt();
        }
        return typedThis;
    }

    /**
     * 获取in表达式 包含括号
     *
     * @param value 集合
     * @return in表达式
     */
    protected IExprSegment inExpression(Collection<?> value) {
        if (CollectionUtils.isEmpty(value)) {
            return () -> "()";
        }
        return () -> value.stream().map(i -> (String) (i))
                .collect(joining(COMMA, LEFT_BRACKET, RIGHT_BRACKET));
    }

    /**
     * 获取in表达式 包含括号
     *
     * @param values 数组
     * @return in表达式
     */
    protected IExprSegment inExpression(Object[] values) {
        if (ArrayUtils.isEmpty(values)) {
            return () -> "()";
        }
        return () -> Arrays.stream(values).map(i -> (String) (i))
                .collect(joining(COMMA, LEFT_BRACKET, RIGHT_BRACKET));
    }

    /**
     * 获取json表达式 包含括号
     *
     * @param column     列
     * @param value      集合
     * @param identifier json key
     * @return in表达式
     */
    protected IExprSegment jsonExpression(String column, Collection<?> value, Object... identifier) {
        if (CollectionUtils.isEmpty(value)) {
            return () -> "()";
        }
        String jsonKey = getExpressionValue(identifier);

        String valueStr = value.stream().map(this::formatParam).collect(joining(COMMA, LEFT_SQ_BRACKET, RIGHT_SQ_BRACKET));

        return () -> LEFT_BRACKET + column + jsonKey + COMMA + SPACE + valueStr + RIGHT_BRACKET;
    }

    /**
     * 获取json表达式 包含括号
     *
     * @param column     列
     * @param value      集合
     * @param identifier json key
     * @return in表达式
     */
    protected IExprSegment jsonExpression(String column, Object value, Object... identifier) {
        if (ObjectUtils.isEmpty(value)) {
            return () -> "()";
        }
        String jsonKey = getExpressionValue(identifier);
        String valueStr = formatParam(value);

        return () -> LEFT_BRACKET + column + jsonKey + COMMA + SPACE + valueStr + RIGHT_BRACKET;
    }

    /**
     * 获取array表达式 包含括号
     *
     * @param column 列
     * @param value  值
     * @return in表达式
     */
    protected IExprSegment arrayExpression(String column, Object value) {
        if (ObjectUtils.isEmpty(value)) {
            return () -> "()";
        }
        String valueStr = formatParam(value);
        return () -> LEFT_BRACKET + column + LEFT_SQ_BRACKET + valueStr + RIGHT_SQ_BRACKET + RIGHT_BRACKET;
    }

    /**
     * 获取array表达式 包含括号
     *
     * @param column 列
     * @param values 数组
     * @return in表达式
     */
    protected IExprSegment arrayExpression(String column, Object... values) {
        if (ArrayUtils.isEmpty(values)) {
            return () -> "()";
        }
        return () -> LEFT_BRACKET + column + Arrays.stream(values).map(this::formatParam).collect(joining(COMMA, LEFT_SQ_BRACKET, RIGHT_SQ_BRACKET)) + RIGHT_BRACKET;
    }

    private String getExpressionValue(Object[] values) {
        return Arrays.stream(values).map(param -> LEFT_SQ_BRACKET + this.formatParam(param) + RIGHT_SQ_BRACKET).collect(joining());
    }

    /**
     * 必要的初始化
     */
    protected void initNeed() {
        paramNameSeq = new AtomicInteger(0);
        expression = new MergeSegments();
    }

    @Override
    public void clear() {
        entity = null;
        paramNameSeq.set(0);
        expression.clear();
    }

    /**
     * 添加 where 片段
     *
     * @param exprSegments IExprSegment 数组
     */
    protected void appendExprSegments(IExprSegment... exprSegments) {
        expression.add(exprSegments);
    }

    @Override
    public String getExprSegment() {
        return expression.getExprSegment();
    }

    @Override
    public MergeSegments getExpression() {
        return expression;
    }


    /**
     * 获取 columnName
     *
     * @param column 列
     * @return 表达式
     */
    protected final ColumnSegment columnToExprSegment(R column) {
        return () -> columnToString(column);
    }

    /**
     * 获取 columnName
     *
     * @param column 列
     * @return 列名
     */
    protected String columnToString(R column) {
        return (String) column;
    }

    /**
     * 获取 columnNames
     *
     * @param columns 多列
     * @return 列名
     */
    protected String columnsToString(R... columns) {
        return Arrays.stream(columns).map(this::columnToString).collect(joining(COMMA));
    }

    /**
     * 多字段转换为逗号 "," 分割字符串
     *
     * @param columns 多字段
     * @return 列名
     */
    protected String columnsToString(List<R> columns) {
        return columns.stream().map(this::columnToString).collect(joining(COMMA));
    }

    @Override
    @SuppressWarnings("all")
    public Children clone() {
        return SerializationUtils.clone(typedThis);
    }

    /**
     * 做事函数
     */
    @FunctionalInterface
    public interface DoSomething {

        void doIt();
    }
}
