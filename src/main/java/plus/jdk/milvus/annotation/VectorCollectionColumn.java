package plus.jdk.milvus.annotation;

import io.milvus.grpc.DataType;
import plus.jdk.milvus.global.DefaultEmbeddingTypeHandler;
import plus.jdk.milvus.global.VectorTypeHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface VectorCollectionColumn {

    /**
     * 字段名
     */
    String name();

    /**
     * 是否是主键
     */
    boolean primary() default false;

    /**
     * 数据类型
     */
    DataType dataType();

    /**
     * 数据向量化处理的handler
     */
    Class<? extends VectorTypeHandler<?, ?>> EmbeddingTypeHandler() default DefaultEmbeddingTypeHandler.class;

    /**
     * 字段描述
     */
    String desc() default "";

    /**
     * 指定向量维度,其他类型无需指定
     */
    int vectorDimension() default 1024;

    /**
     * varchar类型最大长度, 其他类型无需指定
     */
    int maxLength() default 500;

    /**
     * 将字段设置为分区键。分区键字段的值经过哈希处理并分发到不同的逻辑分区。只有 int64 和 varchar 类型字段可以是分区键。主键字段不能是分区键。
     */
    boolean partitionKey() default false;
}
