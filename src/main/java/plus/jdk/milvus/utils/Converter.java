package plus.jdk.milvus.utils;

import java.util.ArrayList;
import java.util.List;

public class Converter {
    public static <T> List<T> objectToList(Object object, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        if (object instanceof List<?>) {
            for (Object o : (List<?>) object) {
                result.add(clazz.cast(o));
            }
        }
        return result;
    }
}