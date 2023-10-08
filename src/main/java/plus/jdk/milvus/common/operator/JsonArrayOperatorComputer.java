package plus.jdk.milvus.common.operator;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import plus.jdk.milvus.utils.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

@AllArgsConstructor
public class JsonArrayOperatorComputer implements IOperatorComputer {

    private String operator;

    @Override
    public String getOperator() {
        return operator;
    }

    @Override
    public String compute(String leftValue, Object rightValue, Object identifier, Class<?> clazz) {
        List<Object> params = Converter.objectToList(rightValue, Object.class);
        List<Object> identifiers = new ArrayList<>(Arrays.asList((Object[]) identifier));

        String jsonKey = identifiers.stream()
                // ["key"] or [0]
                .map(item -> {
                    if (item instanceof String) {
                        // ['item']
                        return LEFT_SQ_BRACKET + SINGLE_QUOTE + item + SINGLE_QUOTE + RIGHT_SQ_BRACKET;
                    } else if (NumberUtils.isParsable(item.toString())) {
                        // [item]
                        return LEFT_SQ_BRACKET + item + RIGHT_SQ_BRACKET;
                    }
                    throw new IllegalArgumentException("identifier item must be String or Number");
                }).collect(joining());
        // column1 = {"params": ["19", "55"]}
        // operator = json_contains_any | json_contains | json_contains_all
        // leftValue = column1
        // jsonKey = ["params"]
        // jsonValue = ["19", "55"]
        String jsonValue = params.stream()
                .map(item -> {
                    if (item instanceof String) {
                        // 'item'
                        return SINGLE_QUOTE + item + SINGLE_QUOTE;
                    } else if (NumberUtils.isParsable(item.toString())) {
                        // item
                        return item.toString();
                    } else {
                        throw new IllegalArgumentException("identifier item must be String or Number");
                    }
                }).collect(joining(COMMA));
        return String.format("%s(%s%s, [%s])", operator, leftValue, jsonKey, jsonValue);
    }
}
