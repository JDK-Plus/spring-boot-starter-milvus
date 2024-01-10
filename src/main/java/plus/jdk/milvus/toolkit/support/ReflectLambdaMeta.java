package plus.jdk.milvus.toolkit.support;


import lombok.extern.slf4j.Slf4j;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.toolkit.ReflectionKit;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;

@Slf4j
public class ReflectLambdaMeta implements LambdaMeta {

    private final SerializedLambda lambda;

    public ReflectLambdaMeta(SerializedLambda lambda) {
        this.lambda = lambda;
    }

    @Override
    public String getImplMethodName() {
        return lambda.getImplMethodName();
    }

}
