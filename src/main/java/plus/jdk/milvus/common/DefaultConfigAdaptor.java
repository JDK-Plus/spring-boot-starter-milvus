package plus.jdk.milvus.common;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class DefaultConfigAdaptor implements IConfigAdaptor {

    private final Gson gson = new Gson();

    @Override
    public <T> String serialize(T data) {
        return gson.toJson(data);
    }

    @Override
    public <T> T deserialize(String data, Type type) {
        return gson.fromJson(data, type);
    }
}
