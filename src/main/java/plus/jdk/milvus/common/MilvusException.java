package plus.jdk.milvus.common;

import lombok.Data;

public class MilvusException extends Exception {
    public MilvusException(String message) {
        super(message);
    }
}
