package plus.jdk.milvus.toolkit;

import plus.jdk.milvus.config.GlobalConfig;
import plus.jdk.milvus.enums.IdType;
import plus.jdk.milvus.global.handler.AnnotationHandler;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Milvus全局缓存工具类
 */
public class GlobalConfigUtils {

    /**
     * 缓存全局信息
     */
    private static final Map<String, GlobalConfig> GLOBAL_CONFIG = new ConcurrentHashMap<>();


    /**
     * 获取默认 MilvusGlobalConfig
     *
     * @return 默认配置
     */
    public static GlobalConfig defaults() {
        return new GlobalConfig().setMilvusConfig(new GlobalConfig.MilvusConfig());
    }

    /**
     * 获取MilvusGlobalConfig (统一所有入口)
     *
     * @param clazz Milvus 容器配置对象
     * @return 全局配置
     */
    public static GlobalConfig getGlobalConfig(Class<?> clazz) {
        Assert.notNull(clazz, "Error: You need Initialize MilvusConfiguration !");
        final String key = Integer.toHexString(clazz.hashCode());
        return CollectionUtils.computeIfAbsent(GLOBAL_CONFIG, key, k -> defaults());
    }

    public static IdType getIdType(Class<?> clazz) {
        return getGlobalConfig(clazz).getMilvusConfig().getIdType();
    }

    public static GlobalConfig.MilvusConfig getDbConfig(Class<?> clazz) {
        return getGlobalConfig(clazz).getMilvusConfig();
    }

//    public static Optional<MetaObjectHandler> getMetaObjectHandler(Class<?> clazz) {
//        return Optional.ofNullable(getGlobalConfig(clazz).getMetaObjectHandler());
//    }

    public static Optional<AnnotationHandler> getAnnotationHandler(Class<?> clazz) {
        return Optional.ofNullable(getGlobalConfig(clazz).getAnnotationHandler());
    }

    public static Class<?> getSuperMapperClass(Class<?> clazz) {
        return getGlobalConfig(clazz).getSuperMapperClass();
    }

    public static boolean isSupperMapperChildren(Class<?> clazz, Class<?> mapperClass) {
        return getSuperMapperClass(clazz).isAssignableFrom(mapperClass);
    }
}