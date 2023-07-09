package plus.jdk.milvus.common.operator;

import lombok.AllArgsConstructor;
import org.springframework.util.CollectionUtils;
import plus.jdk.cli.common.StringUtils;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.wrapper.AbstractLambdaWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 逻辑运算符定义
 */
@AllArgsConstructor
public class LogicOperatorComputer implements IOperatorComputer {

    private String operatorName;

    @Override
    public String compute(String leftValue, Object rightValue, Class<?> clazz) throws MilvusException {
        AbstractLambdaWrapper<?>[] wrappers = (AbstractLambdaWrapper<?>[]) rightValue;
        List<String> expressions = new ArrayList<>();
        for (AbstractLambdaWrapper<?> wrapper : wrappers) {
            expressions.add(wrapper.buildExpression(clazz));
        }
        if(CollectionUtils.isEmpty(expressions)) {
            return "";
        }
        return String.join("%s (%s)", operatorName, String.join(" ", expressions));
    }
}
