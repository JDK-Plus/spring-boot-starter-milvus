package plus.jdk.milvus.global;

import lombok.extern.slf4j.Slf4j;
import plus.jdk.milvus.annotation.EmbeddingHandler;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@EmbeddingHandler
public class DefaultEmbeddingTypeHandler implements EmbeddingTypeHandler<Object, Byte> {

    @Override
    public List<Byte> computeDataVector(Object data) {
        byte[] bytes = data.toString().getBytes(StandardCharsets.UTF_8);
        List<Byte> byteList = new ArrayList<>();
        for (byte value : bytes) {
            byteList.add(value);
        }
        return byteList;
    }
}
