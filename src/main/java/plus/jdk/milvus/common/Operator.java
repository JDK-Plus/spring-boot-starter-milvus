package plus.jdk.milvus.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import plus.jdk.milvus.common.operator.IOperatorComputer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum Operator {
    ne((leftValue, rightValue) -> String.format("%s !== %s", leftValue, rightValue)),
    eq((leftValue, rightValue) -> String.format("%s == %s", leftValue, rightValue)),
    gt((leftValue, rightValue) -> String.format("%s > %s", leftValue, rightValue)),
    ge((leftValue, rightValue) -> String.format("%s >= %s", leftValue, rightValue)),
    lt((leftValue, rightValue) -> String.format("%s < %s", leftValue, rightValue)),
    le((leftValue, rightValue) -> String.format("%s <= %s", leftValue, rightValue)),
    like((leftValue, rightValue) -> String.format("%s like %s", leftValue, rightValue)),
    not_like((leftValue, rightValue) -> String.format("%s not like %s", leftValue, rightValue)),
    and((leftValue, rightValue) -> " and "),
    or((leftValue, rightValue) -> " or "),
    lbracket((leftValue, rightValue) -> "("),
    rbracket((leftValue, rightValue) -> ")"),
    in((leftValue, rightValue) -> {
        List<String> params = new ArrayList<>();
        for(Object object: (Object[]) rightValue) {
            params.add(object.toString());
        }
        return String.format("%s in [%s]" , leftValue, String.join(",", params));
    }),
    ;
    /**
     * 回调
     */
    private final IOperatorComputer iOperatorComputer;
}
