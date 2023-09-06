package plus.jdk.milvus.common;

import lombok.Data;

public class MilvusException extends RuntimeException {

    public MilvusException(String message) {
        super(message);
    }
    public MilvusException(String message, Throwable t) {
        super(message, t);
    }
    public MilvusException(Throwable t) {
        super(t);
    }
}
