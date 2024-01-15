package plus.jdk.milvus.global.handler;

import lombok.extern.slf4j.Slf4j;
import plus.jdk.milvus.global.VectorTypeHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
public class UnknownTypeHandler implements VectorTypeHandler<Object> {

    /**
     * 入库时序列化
     */
    @Override
    public List<Object> serialize(Object data) {
        return Collections.singletonList(data);
    }

    @Override
    public Object deserialize(Collection<Object> data) {
        return null;
    }
}
