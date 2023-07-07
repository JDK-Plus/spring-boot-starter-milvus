package plus.jdk.milvus.common.operator;

import plus.jdk.milvus.common.Operator;

public interface IOperatorComputer {
    String compute(String leftValue, Object rightValue);
}
