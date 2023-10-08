package plus.jdk.milvus.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VectorCollectionName {

    /**
     * @return 表名称
     */
    String name();

    /**
     * @return 表描述
     */
    String description();

    /**
     * @return 指定数据库，若未指定，则使用默认的
     */
    String database() default "";
}
