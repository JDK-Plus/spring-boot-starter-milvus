package plus.jdk.milvus.toolkit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import plus.jdk.milvus.metadata.CollectionDefinition;
import plus.jdk.milvus.metadata.CollectionHelper;
import plus.jdk.milvus.toolkit.support.*;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Locale.ENGLISH;

/**
 * Lambda 解析工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LambdaUtils {

    /**
     * 字段映射
     */
    private static final Map<String, Map<String, ColumnCache>> COLUMN_CACHE_MAP = new ConcurrentHashMap<>();

    /**
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @param <T>  类型，被调用的 Function 对象的目标类型
     * @return 返回解析后的结果
     */
    public static <T> LambdaMeta extract(SFunction<T, ?> func) {
        // 1. IDEA 调试模式下 lambda 表达式是一个代理
        if (func instanceof Proxy) {
            return new IdeaProxyLambdaMeta((Proxy) func);
        }
        // 2. 反射读取
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            return new ReflectLambdaMeta((SerializedLambda) setAccessible(method).invoke(func), func.getClass().getClassLoader());
        } catch (Exception e) {
            // 3. 反射失败使用序列化的方式读取
            return new ShadowLambdaMeta(plus.jdk.milvus.toolkit.support.SerializedLambda.extract(func));
        }
    }

    /**
     * 格式化 key 将传入的 key 变更为大写格式
     *
     * <pre>
     *     Assert.assertEquals("USERID", formatKey("userId"))
     * </pre>
     *
     * @param key key
     * @return 大写的 key
     */
    public static String formatKey(String key) {
        return key.toUpperCase(ENGLISH);
    }

    /**
     * 设置可访问对象的可访问权限为 true
     *
     * @param object 可访问的对象
     * @param <T>    类型
     * @return 返回设置后的对象
     */
    public static <T extends AccessibleObject> T setAccessible(T object) {
        return AccessController.doPrivileged(new SetAccessibleAction<>(object));
    }

    /**
     * 将传入的表信息加入缓存
     *
     * @param collectionDefinition 表信息
     */
    public static void installCache(CollectionDefinition collectionDefinition) {
        COLUMN_CACHE_MAP.put(collectionDefinition.getEntityType().getName(), createColumnCacheMap(collectionDefinition));
    }

    /**
     * 缓存实体字段 MAP 信息
     *
     * @param info 表信息
     * @return 缓存 map
     */
    private static Map<String, ColumnCache> createColumnCacheMap(CollectionDefinition info) {
        Map<String, ColumnCache> map;

        if (info.havePK()) {
            map = CollectionUtils.newHashMapWithExpectedSize(info.getFieldList().size() + 1);
            map.put(formatKey(info.getKeyProperty()), new ColumnCache(info.getKeyColumn()));
        } else {
            map = CollectionUtils.newHashMapWithExpectedSize(info.getFieldList().size());
        }

        info.getFieldList().forEach(i ->
                map.put(formatKey(i.getProperty()), new ColumnCache(i.getName()))
        );
        return map;
    }

    /**
     * 获取实体对应字段 MAP
     *
     * @param clazz 实体类
     * @return 缓存 map
     */
    public static Map<String, ColumnCache> getColumnMap(Class<?> clazz) {
        if (COLUMN_CACHE_MAP.containsKey(clazz.getName())) {
            return COLUMN_CACHE_MAP.get(clazz.getName());
        }
        CollectionDefinition info = CollectionHelper.getCollectionInfo(clazz);
        if (info == null) {
            return null;
        }
        Map<String, ColumnCache> columnCacheMap = createColumnCacheMap(info);
        COLUMN_CACHE_MAP.put(clazz.getName(), columnCacheMap);
        return columnCacheMap;
//        return COLUMN_CACHE_MAP.computeIfAbsent(clazz.getName(), key -> {
//            CollectionDefinition info = CollectionHelper.getCollectionInfo(clazz);
//            return info == null ? null : createColumnCacheMap(info);
//        });
    }
}
