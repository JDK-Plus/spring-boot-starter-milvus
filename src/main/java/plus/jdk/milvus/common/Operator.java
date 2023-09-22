package plus.jdk.milvus.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import plus.jdk.milvus.common.operator.*;
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
    in(new InOperatorComputer()),
    contains(new JsonArrayOperatorComputer("json_contains")),
    contains_all(new JsonArrayOperatorComputer("json_contains_all")),
    contains_any(new JsonArrayOperatorComputer("json_contains_any")),
    ;
    /**
     * 回调
     */
    private final IOperatorComputer iOperatorComputer;
}
