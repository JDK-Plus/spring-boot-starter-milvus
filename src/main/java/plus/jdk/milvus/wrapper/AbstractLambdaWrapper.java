package plus.jdk.milvus.wrapper;

import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.Data;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.reflection.property.PropertyNamer;

public class AbstractLambdaWrapper<T> {

    private <R> String getColumnName(SFunction<T, R> column) {
        LambdaMeta lambdaMeta = LambdaUtils.extract(column);
        return PropertyNamer.methodToProperty(lambdaMeta.getImplMethodName());
    }

    public <R> void eq(SFunction<T, R> column, R value) {

    }

    @Data
    public static class TestData {

        Integer name;
    }

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        SFunction<TestData, Integer> column = TestData::getName;
        AbstractLambdaWrapper<TestData> wrapper = new AbstractLambdaWrapper<>();
        wrapper.getColumnName(TestData::getName);
        System.out.println("ssssssssss");
    }
}
