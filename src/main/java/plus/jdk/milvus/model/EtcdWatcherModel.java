package plus.jdk.milvus.model;

import io.milvus.jmilvus.Watch;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.util.ReflectionUtils;
import plus.jdk.milvus.annotation.milvusNode;

import java.lang.reflect.Field;

@Data
@AllArgsConstructor
public class milvusWatcherModel<T> {

    /**
     * 字段注解
     */
    private milvusNode milvusNode;

    /**
     * 对应的bean实例
     */
    private Object beanInstance;

    /**
     * 需要刷新的字段
     */
    private Field field;

    /**
     * 字段类型
     */
    private Class<T> clazz;

    /**
     * watch对象
     */
    private Watch.Watcher watcher;

    public milvusWatcherModel(milvusNode milvusNode, Object beanInstance, Field field, Class<T> clazz) {
        this.milvusNode = milvusNode;
        this.beanInstance = beanInstance;
        this.field = field;
        this.clazz = clazz;
    }

    public void setFieldValue(Object value) {
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, beanInstance, value);
    }
}
