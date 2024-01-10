package plus.jdk.milvus.conditions.search;


import java.io.Serializable;

public interface Search<Children, T, R> extends Serializable {
    String getExprSelect();
}
