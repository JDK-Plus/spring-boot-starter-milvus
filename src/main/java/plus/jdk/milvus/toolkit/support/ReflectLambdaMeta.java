package plus.jdk.milvus.toolkit.support;


import lombok.extern.slf4j.Slf4j;
import plus.jdk.milvus.toolkit.ClassUtils;
import plus.jdk.milvus.toolkit.StringPool;

import java.lang.invoke.SerializedLambda;

@Slf4j
public class ReflectLambdaMeta implements LambdaMeta {

    private final SerializedLambda lambda;

    private final ClassLoader classLoader;

    public ReflectLambdaMeta(SerializedLambda lambda, ClassLoader classLoader) {
        this.lambda = lambda;
        this.classLoader = classLoader;
    }

    @Override
    public String getImplMethodName() {
        return lambda.getImplMethodName();
    }

    @Override
    public Class<?> getInstantiatedClass() {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(StringPool.SEMICOLON)).replace(StringPool.SLASH, StringPool.DOT);
        return ClassUtils.toClassConfident(instantiatedType, this.classLoader);
    }

}
