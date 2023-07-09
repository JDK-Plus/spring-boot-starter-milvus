package plus.jdk.milvus.common.operator;

import plus.jdk.milvus.common.MilvusException;

public interface IOperatorComputer {
    String compute(String leftValue, Object rightValue, Class<?> clazz) throws MilvusException;
}
