package plus.jdk.milvus.common;

/**
 * <p>
 * Create by hcl at 2021/7/7
 */
public class ShadowLambdaMeta implements LambdaMeta {
    private final SerializedLambda lambda;

    public ShadowLambdaMeta(SerializedLambda lambda) {
        this.lambda = lambda;
    }

    @Override
    public String getImplMethodName() {
        return lambda.getImplMethodName();
    }

    @Override
    public Class<?> getInstantiatedClass() {
        String instantiatedMethodType = lambda.getInstantiatedMethodType();
        String instantiatedType = instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(StringPool.SEMICOLON)).replace(StringPool.SLASH, StringPool.DOT);
        return ClassUtils.toClassConfident(instantiatedType, lambda.getCapturingClass().getClassLoader());
    }

}
