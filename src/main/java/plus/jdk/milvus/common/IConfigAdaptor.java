package plus.jdk.milvus.common;

import java.lang.reflect.Type;

public interface IConfigAdaptor {

    /**
     * 序列化数据
     */
    <T> String serialize(T data);

    /**
     * 反序列化数据
     */
    <T> T deserialize(String data, Type type);
}
