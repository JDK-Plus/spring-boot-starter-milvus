package plus.jdk.milvus.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CollectionDefinition {
    /**
     * 表名称
     */
    private String name;

    /**
     * 表描述
     */
    private String description;

    /**
     * 指定数据库，若未指定，则使用默认的
     */
    private String database;

    /**
     * 类型
     */
    private Class<?> clazz;

    /**
     * 表字段
     */
    private List<CollectionColumnDefinition> columns = new ArrayList<>();

    public CollectionColumnDefinition getPrimaryColumn() {
        for(CollectionColumnDefinition collectionColumnDefinition :columns) {
            if(collectionColumnDefinition.getPrimary()) {
                return collectionColumnDefinition;
            }
        }
        return null;
    }
}
