package plus.jdk.milvus.conditions;

import java.io.Serializable;

/**
 * Expr 片段接口
 */
@FunctionalInterface
public interface IExprSegment extends Serializable {

    /**
     * Expr 片段
     */
    String getExprSegment();
}