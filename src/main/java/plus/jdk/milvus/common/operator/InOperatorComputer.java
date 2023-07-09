package plus.jdk.milvus.common.operator;

import lombok.AllArgsConstructor;
import plus.jdk.milvus.common.MilvusException;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class InOperatorComputer implements IOperatorComputer {

    @Override
    public String compute(String leftValue, Object rightValue, Class<?> clazz) throws MilvusException {
        List<String> params = new ArrayList<>();
        for (Object object : (Object[]) rightValue) {
            params.add(String.valueOf(formatRvalue(object)));
        }
        return String.format("%s in [%s]", leftValue, String.join(",", params));
    }
}
