package plus.jdk.milvus.common.operator;

import lombok.AllArgsConstructor;
import plus.jdk.milvus.common.MilvusException;

@AllArgsConstructor
public class CompareOperatorComputer implements IOperatorComputer {

    private String operator;

    @Override
    public String getOperator() {
        return operator;
    }

    @Override
    public String compute(String leftValue, Object rightValue, Object identifier, Class<?> clazz) throws MilvusException {
        return String.format("%s %s %s", leftValue, operator, this.formatRvalue(rightValue));
    }
}
