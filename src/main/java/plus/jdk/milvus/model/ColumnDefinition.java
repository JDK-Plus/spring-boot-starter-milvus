package plus.jdk.milvus.model;

import io.milvus.grpc.DataType;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.Data;
import plus.jdk.milvus.global.VectorTypeHandler;
import plus.jdk.milvus.toolKit.support.SFunction;

import java.lang.reflect.Field;

@Data
public class ColumnDefinition {

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
     * 数据向量化处理的handler
     */
    private VectorTypeHandler<?, ?> vectorTypeHandler;

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
    private Integer maxLength = 128;

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

    public boolean canBePartitionKey() {
        return dataType == DataType.Int64 || dataType == DataType.VarChar;
    }

    public boolean vectorColumn() {
        return DataType.BinaryVector == dataType || DataType.FloatVector == dataType;
    }
}
