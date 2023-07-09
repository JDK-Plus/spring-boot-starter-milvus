package plus.jdk.milvus.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import plus.jdk.milvus.common.operator.CompareOperatorComputer;
import plus.jdk.milvus.common.operator.IOperatorComputer;
import plus.jdk.milvus.common.operator.LogicOperatorComputer;
import plus.jdk.milvus.wrapper.AbstractLambdaWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum Operator {
    ne(new CompareOperatorComputer("!=")),
    eq(new CompareOperatorComputer("==")),
    gt(new CompareOperatorComputer(">")),
    ge(new CompareOperatorComputer(">=")),
    lt(new CompareOperatorComputer("<")),
    le(new CompareOperatorComputer("<=")),
    like(new CompareOperatorComputer("like")),
    not_like(new CompareOperatorComputer("not like")),
    and(new LogicOperatorComputer("and")),
    or(new LogicOperatorComputer("or")),
    not(new LogicOperatorComputer("not")),
    in((leftValue, rightValue, clazz) -> {
        List<String> params = new ArrayList<>();
        for (Object object : (Object[]) rightValue) {
            params.add(object.toString());
        }
        return String.format("%s in [%s]", leftValue, String.join(",", params));
    }),
    ;
    /**
     * 回调
     */
    private final IOperatorComputer iOperatorComputer;
}
