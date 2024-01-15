package plus.jdk.milvus.annotation;

import io.milvus.grpc.DataType;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import plus.jdk.milvus.global.VectorTypeHandler;
import plus.jdk.milvus.global.handler.UnknownTypeHandler;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VectorCollectionColumn {

    /**
     * @return 字段名
     */
    String name();

    /**
     * @return 是否是主键
     */
    boolean primary() default false;

    /**
     * @return 数据类型
     */
    DataType dataType();

    /**
     * 当数据类型【dataType】为 {@link DataType.Array} 时，需要指定数组元素的类型【elementType】
     *
     * @return 数组类型字段的元素类型
     */
    DataType elementType() default DataType.None;

    /**
     * @return 数组类型字段的最大容量
     */
    int maxCapacity() default 32;

    /**
     * @return 数据向量化处理的handler
     */
    Class<? extends VectorTypeHandler<?>> embeddingTypeHandler() default UnknownTypeHandler.class;

    /**
     * @return 字段描述
     */
    String desc() default "";

    String property() default "";

    /**
     * @return 指定向量维度, 其他类型无需指定
     */
    int vectorDimension() default 1024;

    /**
     * @return varchar类型最大长度, 其他类型无需指定
     */
    int maxLength() default 512;

    /**
     * @return 将字段设置为分区键。分区键字段的值经过哈希处理并分发到不同的逻辑分区。只有 int64 和 varchar 类型字段可以是分区键。主键字段不能是分区键。
     */
    boolean partitionKey() default false;

    /**
     * @return 是否基于该字段创建索引
     */
    boolean index() default false;

    /**
     * @return 索引类型
     * <a href="https://milvus.io/docs/index.md">...</a>
     */
    IndexType indexType() default IndexType.HNSW;

    /**
     * @return 度量类型
     * <a href="https://milvus.io/docs/metric.md">...</a>
     */
    MetricType metricType() default MetricType.L2;
}
