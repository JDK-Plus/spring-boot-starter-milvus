package plus.jdk.milvus.enums;


import lombok.AllArgsConstructor;
import plus.jdk.milvus.conditions.IExprSegment;
import plus.jdk.milvus.toolkit.StringPool;

/**
 * Expr 保留关键字枚举
 */
@AllArgsConstructor
public enum ExprKeyword implements IExprSegment {
    AND("and"),
    OR("or"),
    NOT("not"),
    IN("in"),
    NOT_IN("not in"),
    LIKE("like"),
    NOT_LIKE("not like"),
    EQ("=="),
    NE("!="),
    GT(StringPool.RIGHT_CHEV),
    GE(">="),
    LT(StringPool.LEFT_CHEV),
    LE("<="),
    JSON("json_contains"),
    JSON_ALL("json_contains_all"),
    JSON_ANY("json_contains_any"),
    ARRAY("array_contains"),
    ARRAY_ALL("array_contains_all"),
    ARRAY_ANY("array_contains_any"),
    ARRAY_LENGTH("array_length"),
    ;

    private final String keyword;

    @Override
    public String getExprSegment() {
        return this.keyword;
    }
}
