package plus.jdk.milvus.config;

import lombok.Data;
import lombok.experimental.Accessors;
import plus.jdk.milvus.enums.IdType;
import plus.jdk.milvus.global.handler.AnnotationHandler;
import plus.jdk.milvus.global.handler.PostInitCollectionInfoHandler;
import plus.jdk.milvus.incrementer.IdentifierGenerator;
import plus.jdk.milvus.record.VectorModelRepository;

import java.io.Serializable;

/**
 * Milvus 全局缓存
 */
@Data
@Accessors(chain = true)
public class GlobalConfig implements Serializable {
    /**
     * 是否开启 LOGO
     */
    private boolean banner = true;
    /**
     * 数据库相关配置
     */
    private MilvusConfig milvusConfig;
    /**
     * Mapper父类
     */
    private Class<?> superMapperClass = VectorModelRepository.class;
    /**
     * 注解控制器
     */
    private AnnotationHandler annotationHandler = new AnnotationHandler() {
    };
    /**
     * 参与 TableInfo 的初始化
     */
    private PostInitCollectionInfoHandler postInitCollectionInfoHandler = new PostInitCollectionInfoHandler() {
    };
    /**
     * 主键生成器
     */
    private IdentifierGenerator identifierGenerator;

    @Data
    public static class MilvusConfig {
        /**
         * 主键类型
         */
        private IdType idType = IdType.ASSIGN_ID;
        /**
         * 表名前缀
         */
        private String collectionPrefix;
        /**
         * db字段 format
         * <p>
         * 例: `%s`
         * <p>
         * 对主键无效
         */
        private String columnFormat;
        /**
         * db 表 format
         * <p>
         * 例: `%s`
         * <p>
         */
        private String collectionFormat;
        /**
         * entity 的字段(property)的 format,只有在 column as property 这种情况下生效
         * <p>
         * 例: `%s`
         * <p>
         * 对主键无效
         */
        private String propertyFormat;
        /**
         * 表名是否使用驼峰转下划线命名,只对表名生效
         */
        private boolean tableUnderline = true;
        /**
         * 大写命名,对表名和字段名均生效
         */
        private boolean capitalMode = false;
        /**
         * 逻辑删除全局属性名
         */
        private String logicDeleteField;
        /**
         * 逻辑删除全局值（默认 1、表示已删除）
         */
        private String logicDeleteValue = "1";
        /**
         * 逻辑未删除全局值（默认 0、表示未删除）
         */
        private String logicNotDeleteValue = "0";
    }
}