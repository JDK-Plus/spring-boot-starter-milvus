package plus.jdk.milvus.common;

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
