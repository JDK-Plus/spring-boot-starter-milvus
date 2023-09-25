package plus.jdk.milvus.common.operator;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class JsonArrayOperatorComputer implements IOperatorComputer {

    private String operator;

    @Override
    public String compute(String leftValue, Object rightValue, Class<?> clazz) {
        List<String> params = new ArrayList<>();
        for (Object object : (Object[]) rightValue) {
            params.add(String.valueOf(object));
        }
        if (params.size() == 1) {
            return String.format("%s(%s, %s)", operator, leftValue, params.get(0));
        }
        rightValue = String.format("[%s]", String.join(",", params));
        return String.format("%s(%s, %s)", operator, leftValue, rightValue);
    }
}
