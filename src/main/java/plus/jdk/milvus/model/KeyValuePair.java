package plus.jdk.milvus.model;


import io.milvus.jmilvus.KeyValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyValuePair<T> {

    private String key;

    private T value;

    private KeyValue kv;
}
