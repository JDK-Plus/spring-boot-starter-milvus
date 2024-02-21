package plus.jdk.milvus.common.operator;

import plus.jdk.milvus.common.MilvusException;

public interface IOperatorComputer {

    String compute(String leftValue, Object rightValue, Class<?> clazz) throws MilvusException;

    default Object formatRvalue(Object rvalue) {
        if(!(rvalue instanceof Integer || rvalue instanceof Long || rvalue instanceof Float || rvalue instanceof Double)) {
            return String.format("\"%s\"", rvalue);
        }
        return rvalue;
    }
}
