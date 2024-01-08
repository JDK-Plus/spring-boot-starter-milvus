package plus.jdk.milvus.toolKit;


import plus.jdk.milvus.common.MilvusException;

/**
 * 异常辅助工具类
 */
public final class ExceptionUtils {

    private ExceptionUtils() {
    }

    /**
     * 返回一个新的异常，统一构建，方便统一处理
     *
     * @param msg    消息
     * @param t      异常信息
     * @param params 参数
     * @return 返回异常
     */
    public static MilvusException mpe(String msg, Throwable t, Object... params) {
        return new MilvusException(String.format(msg, params), t);
    }

    /**
     * 重载的方法
     *
     * @param msg    消息
     * @param params 参数
     * @return 返回异常
     */
    public static MilvusException mpe(String msg, Object... params) {
        return new MilvusException(String.format(msg, params));
    }

    /**
     * 重载的方法
     *
     * @param t 异常
     * @return 返回异常
     */
    public static MilvusException mpe(Throwable t) {
        return new MilvusException(t);
    }

    public static void throwMpe(boolean condition, String msg, Object... params) {
        if (condition) {
            throw mpe(msg, params);
        }
    }
}
