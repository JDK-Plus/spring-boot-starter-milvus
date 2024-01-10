package plus.jdk.milvus.enums;


import lombok.AllArgsConstructor;
import plus.jdk.milvus.conditions.IExprSegment;
import plus.jdk.milvus.toolkit.StringPool;

/**
 * Expr 保留关键字枚举
 */
@AllArgsConstructor
public enum ExprKeyword implements IExprSegment {
    AND("AND"),
    OR("OR"),
    NOT("NOT"),
    IN("IN"),
    NOT_IN("NOT IN"),
    LIKE("LIKE"),
    NOT_LIKE("NOT LIKE"),
    EQ(StringPool.EQUALS),
    NE("!="),
    GT(StringPool.RIGHT_CHEV),
    GE(">="),
    LT(StringPool.LEFT_CHEV),
    LE("<="),
    JSON("JSON_CONTAINS"),
    JSON_ALL("JSON_CONTAINS_ALL"),
    JSON_ANY("JSON_CONTAINS_ANY"),
    ARRAY("ARRAY_CONTAINS"),
    ARRAY_ALL("ARRAY_CONTAINS_ALL"),
    ARRAY_ANY("ARRAY_CONTAINS_ANY"),
    ARRAY_LENGTH("ARRAY_LENGTH"),
    ;

    private final String keyword;

    @Override
    public String getExprSegment() {
        return this.keyword;
    }
}
