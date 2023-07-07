package plus.jdk.milvus.model;

import lombok.Data;
import plus.jdk.milvus.record.VectorModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Data
public class TableDefinition {
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
    private List<TableColumnDefinition> columns = new ArrayList<>();
}
