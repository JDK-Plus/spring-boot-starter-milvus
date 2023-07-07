package plus.jdk.milvus.global;

import lombok.extern.slf4j.Slf4j;
import plus.jdk.milvus.annotation.EmbeddingHandler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@EmbeddingHandler
public class DefaultEmbeddingTypeHandler implements VectorTypeHandler<Object, Object> {

    @Override
    public List<Object> serialize(Object data) {
        return Collections.singletonList(data);
    }

    @Override
    public Object deserialize(List<Object> data) {
        return null;
    }
}
