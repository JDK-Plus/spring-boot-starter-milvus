package plus.jdk.milvus.metadata;

import io.milvus.grpc.DataType;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.Data;
import plus.jdk.milvus.annotation.VectorCollectionColumn;
import plus.jdk.milvus.config.GlobalConfig;
import plus.jdk.milvus.global.VectorTypeHandler;
import plus.jdk.milvus.toolkit.StringUtils;
import plus.jdk.milvus.toolkit.support.SFunction;

import java.lang.reflect.Field;

@Data
public class ColumnDefinition {

    /**
     * 属性名
     */
    private final String property;
    /**
     * 是否是主键
     */
    private Boolean primary;
    /**
     * 字段名
     */
    private String name;
    /**
     * 数据类型
     */
    private DataType dataType;
    /**
     * 数组类型字段的元素类型
     */
    private DataType elementType;

    /**
     * 数组类型字段的最大容量
     */
    private int maxCapacity;

    /**
     * 数据向量化处理的handler
     */
    private VectorTypeHandler<?> vectorTypeHandler;


    /**
     * 是否是基本数据类型
     */
//    private final boolean isPrimitive;
    /**
     * 属性是否是 CharSequence 类型
     */
//    private final boolean isCharSequence;

    /**
     * 字段描述
     */
    private String desc;

    /**
     *
     */
    private SFunction<?, ?> column;


    /**
     * 字段
     */
    private Field field;

    /**
     * 向量维度,其他类型不用指定
     */
    private Integer vectorDimension = 1024;

    /**
     * varchar类型最大长度, 其他类型不用指定
     */
    private Integer maxLength = 512;

    /**
     * 是否将该字段置为分区键
     */
    private Boolean partitionKey = false;

    /**
     * 是否基于该字段创建索引
     */
    private boolean index = false;

    /**
     * 索引类型
     * <a href="https://milvus.io/docs/index.md">...</a>
     */
    private IndexType indexType = IndexType.HNSW;

    /**
     * 度量类型
     * <a href="https://milvus.io/docs/metric.md">...</a>
     */
    private MetricType metricType = MetricType.L2;

    public ColumnDefinition() {
        this.property = "";
    }

    /**
     * 全新的 存在 CollectionField 注解时使用的构造函数
     *
     * @param globalConfig         全局配置
     * @param collectionDefinition collection信息
     * @param field                字段
     * @param collectionColumn     字段注解
     * @param vectorTypeHandler    向量化处理器
     * @param existTableLogic      是否存在逻辑删除
     */
    public ColumnDefinition(GlobalConfig globalConfig, CollectionDefinition collectionDefinition, Field field, VectorCollectionColumn collectionColumn,
                            VectorTypeHandler<?> vectorTypeHandler, boolean existTableLogic) {

        GlobalConfig.MilvusConfig dbConfig = globalConfig.getMilvusConfig();
        field.setAccessible(true);
        this.field = field;
        this.property = field.getName();
        this.desc = collectionColumn.desc();
        this.primary = collectionColumn.primary();
        this.dataType = collectionColumn.dataType();
        this.vectorTypeHandler = vectorTypeHandler;
        this.partitionKey = collectionColumn.partitionKey();
        this.vectorDimension = collectionColumn.vectorDimension();
        this.maxLength = collectionColumn.maxLength();
        this.index = collectionColumn.index();
        this.indexType = collectionColumn.indexType();
        this.metricType = collectionColumn.metricType();
        this.elementType = collectionColumn.elementType();
        this.maxCapacity = collectionColumn.maxCapacity();
//        this.propertyType = reflector.getGetterType(this.property);
//        this.isPrimitive = this.propertyType.isPrimitive();
//        this.isCharSequence = StringUtils.isCharSequence(this.propertyType);
//        this.fieldFill = collectionColumn.fill();
//        this.withInsertFill = this.fieldFill == FieldFill.INSERT || this.fieldFill == FieldFill.INSERT_UPDATE;
//        this.withUpdateFill = this.fieldFill == FieldFill.UPDATE || this.fieldFill == FieldFill.INSERT_UPDATE;
//        this.initLogicDelete(globalConfig, field, existTableLogic);

        String column = collectionColumn.name();
        if (StringUtils.isBlank(column)) {
            column = this.property;
            if (collectionDefinition.isUnderCamel()) {
                /* 开启字段下划线申明 */
                column = StringUtils.camelToUnderline(column);
            }
            if (dbConfig.isCapitalMode()) {
                /* 开启字段全大写申明 */
                column = column.toUpperCase();
            }
        }
        String columnFormat = dbConfig.getColumnFormat();
        if (StringUtils.isNotBlank(columnFormat)) {
            column = String.format(columnFormat, column);
        }

        this.name = column;

//        this.insertStrategy = this.chooseFieldStrategy(collectionColumn.insertStrategy(), dbConfig.getInsertStrategy());
//        this.updateStrategy = this.chooseFieldStrategy(collectionColumn.updateStrategy(), dbConfig.getUpdateStrategy());

    }

    /**
     * 不存在 CollectionField 注解时, 使用的构造函数
     *
     * @param globalConfig         全局配置
     * @param collectionDefinition collection信息
     * @param field                字段
     * @param existCollectionLogic 是否存在逻辑删除
     */
    public ColumnDefinition(GlobalConfig globalConfig, CollectionDefinition collectionDefinition, Field field,
                            boolean existCollectionLogic) {
        field.setAccessible(true);
        this.field = field;
        this.property = field.getName();
//        this.propertyType = reflector.getGetterType(this.property);
//        this.isPrimitive = this.propertyType.isPrimitive();
//        this.isCharSequence = StringUtils.isCharSequence(this.propertyType);
        GlobalConfig.MilvusConfig dbConfig = globalConfig.getMilvusConfig();
//        this.insertStrategy = dbConfig.getInsertStrategy();
//        this.updateStrategy = dbConfig.getUpdateStrategy();
//        this.initLogicDelete(globalConfig, field, existCollectionLogic);

        String column = this.property;
        if (collectionDefinition.isUnderCamel()) {
            /* 开启字段下划线申明 */
            column = StringUtils.camelToUnderline(column);
        }
        if (dbConfig.isCapitalMode()) {
            /* 开启字段全大写申明 */
            column = column.toUpperCase();
        }

        String columnFormat = dbConfig.getColumnFormat();
        if (StringUtils.isNotBlank(columnFormat)) {
            column = String.format(columnFormat, column);
        }

        this.name = column;
    }

    @SuppressWarnings("unchecked")
    public <T> VectorTypeHandler<T> getVectorTypeHandler() {
        return (VectorTypeHandler<T>) vectorTypeHandler;
    }

    public boolean canBePartitionKey() {
        return dataType == DataType.Int64 || dataType == DataType.VarChar;
    }

    public boolean vectorColumn() {
        return DataType.BinaryVector == dataType || DataType.FloatVector == dataType;
    }
}
