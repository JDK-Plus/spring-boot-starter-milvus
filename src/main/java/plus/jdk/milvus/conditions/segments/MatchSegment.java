package plus.jdk.milvus.conditions.segments;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import plus.jdk.milvus.conditions.IExprSegment;
import plus.jdk.milvus.enums.ExprKeyword;
import plus.jdk.milvus.enums.WrapperKeyword;

import java.util.function.Predicate;

/**
 * 匹配片段
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum MatchSegment {
    NOT(i -> i == ExprKeyword.NOT),
    AND(i -> i == ExprKeyword.AND),
    OR(i -> i == ExprKeyword.OR),
    AND_OR(i -> i == ExprKeyword.AND || i == ExprKeyword.OR),
    APPLY(i -> i == WrapperKeyword.APPLY),
    JSON(i -> i == ExprKeyword.JSON || i == ExprKeyword.JSON_ALL || i == ExprKeyword.JSON_ANY),
    ARRAY(i -> i == ExprKeyword.ARRAY || i == ExprKeyword.ARRAY_ALL || i == ExprKeyword.ARRAY_ANY || i == ExprKeyword.ARRAY_LENGTH),
    ;

    private final Predicate<IExprSegment> predicate;

    public boolean match(IExprSegment segment) {
        return getPredicate().test(segment);
    }
}
