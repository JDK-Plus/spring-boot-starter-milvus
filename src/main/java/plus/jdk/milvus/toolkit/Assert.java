package plus.jdk.milvus.toolkit;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 断言类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Assert {

    /**
     * 断言这个 boolean 为 true
     * <p>为 false 则抛出异常</p>
     *
     * @param expression boolean 值
     * @param message    消息
     * @param params     参数
     */
    public static void isTrue(boolean expression, String message, Object... params) {
        if (!expression) {
            throw ExceptionUtils.mpe(message, params);
        }
    }

    /**
     * 断言这个 object 不为 null
     * <p>为 null 则抛异常</p>
     *
     * @param object  对象
     * @param message 消息
     * @param params  参数
     */
    public static void notNull(Object object, String message, Object... params) {
        isTrue(object != null, message, params);
    }

    /**
     * 断言这个 map 为 empty
     * <p>为 empty 则抛异常</p>
     *
     * @param map     集合
     * @param message 消息
     * @param params  参数
     */
    public static void isEmpty(Map<?, ?> map, String message, Object... params) {
        isTrue(CollectionUtils.isEmpty(map), message, params);
    }
}
