package plus.jdk.milvus.incrementer;


import plus.jdk.milvus.toolkit.IdWorker;
import plus.jdk.milvus.toolkit.StringUtils;

/**
 * Id生成器接口
 */
public interface IdentifierGenerator {

    /**
     * 判断是否分配 ID
     *
     * @param idValue 主键值
     * @return true 分配 false 无需分配
     */
    default boolean assignId(Object idValue) {
        return StringUtils.checkValNull(idValue);
    }

    /**
     * 生成Id
     *
     * @param entity 实体
     * @return id
     */
    Number nextId(Object entity);

    /**
     * 生成uuid
     *
     * @param entity 实体
     * @return uuid
     */
    default String nextUUID(Object entity) {
        return IdWorker.get32UUID();
    }
}
