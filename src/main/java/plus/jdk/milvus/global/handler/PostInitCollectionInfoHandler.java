package plus.jdk.milvus.global.handler;

import plus.jdk.milvus.metadata.CollectionDefinition;
import plus.jdk.milvus.metadata.ColumnDefinition;

/**
 * 初始化 CollectionInfo 同时进行一些操作
 *
 * @author miemie
 * @since 2022-09-20
 */
public interface PostInitCollectionInfoHandler {

    /**
     * 提供对 CollectionInfo 增强的能力
     *
     * @param entityType 实体类型
     * @return {@link CollectionDefinition}
     */
    default CollectionDefinition creteCollectionInfo(Class<?> entityType) {
        return new CollectionDefinition(entityType);
    }

    /**
     * 参与 CollectionInfo 初始化
     *
     * @param tableInfo TableInfo
     */
    default void postCollectionInfo(CollectionDefinition tableInfo) {
        // ignore
    }

    /**
     * 参与 CollectionFieldInfo 初始化
     *
     * @param fieldInfo TableFieldInfo
     */
    default void postFieldInfo(ColumnDefinition fieldInfo) {
        // ignore
    }
}