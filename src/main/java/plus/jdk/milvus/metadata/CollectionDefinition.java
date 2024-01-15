package plus.jdk.milvus.metadata;

import lombok.Data;
import plus.jdk.milvus.toolkit.StringUtils;

import java.util.Collections;
import java.util.List;

@Data
public class CollectionDefinition {

    /**
     * 实体类型
     */
    private Class<?> entityType;
    /**
     * 表名称
     */
    private String name;

    /**
     * 表描述
     */
    private String description;

    /**
     * 表主键ID 字段名
     */
    private String keyColumn;

    /**
     * 表主键ID 属性名
     */
    private String keyProperty;

    /**
     * 是否开启下划线转驼峰
     * <p>
     * 未注解指定字段名的情况下,用于自动从 property 推算 column 的命名
     */
    private boolean underCamel = true;

    /**
     * 表字段信息列表
     */
    private List<ColumnDefinition> columns;

    /**
     * 指定数据库，若未指定，则使用默认的
     */
    private String database;

    /**
     * 类型
     */
    private Class<?> clazz;

    /**
     * @param entityType 实体类型
     */
    public CollectionDefinition(Class<?> entityType) {
        this.entityType = entityType;
    }

    public ColumnDefinition getPrimaryColumn() {
        for (ColumnDefinition columnDefinition : columns) {
            if (Boolean.TRUE.equals(columnDefinition.getPrimary())) {
                return columnDefinition;
            }
        }
        return null;
    }

    /**
     * 是否有主键
     *
     * @return 是否有
     */
    public boolean havePK() {
        return StringUtils.isNotBlank(keyColumn);
    }

    public List<ColumnDefinition> getFieldList() {
        return Collections.unmodifiableList(columns);
    }

    public ColumnDefinition getColumnByColumnName(String columnName) {
        for (ColumnDefinition columnDefinition : columns) {
            if (columnDefinition.getName().equals(columnName)) {
                return columnDefinition;
            }
        }
        return null;
    }
}
