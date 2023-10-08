package plus.jdk.milvus.common.operator;

import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.common.StringPool;

public interface IOperatorComputer extends StringPool {

    String compute(String leftValue, Object rightValue, Object identifier, Class<?> clazz) throws MilvusException;

    String getOperator();

    default Object formatRvalue(Object rvalue) {
        if (rvalue instanceof String) {
            return String.format("\"%s\"", rvalue);
        }
        return rvalue;
    }
}
