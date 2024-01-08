package plus.jdk.milvus.conditions.query;


import java.io.Serializable;

public interface Query<Children, T, R> extends Serializable {
    String getExprSelect();
}
