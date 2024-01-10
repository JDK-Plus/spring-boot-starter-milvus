package plus.jdk.milvus.conditions;

import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.common.PropertyNamer;
import plus.jdk.milvus.record.VectorModel;
import plus.jdk.milvus.toolkit.LambdaUtils;
import plus.jdk.milvus.toolkit.support.ColumnCache;
import plus.jdk.milvus.toolkit.support.LambdaMeta;
import plus.jdk.milvus.toolkit.support.SFunction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLambdaWrapper<T extends VectorModel<? extends VectorModel<?>>, C extends AbstractLambdaWrapper<T, C>>
        extends AbstractWrapper<T, SFunction<T, ?>, C> {

    private final Map<String, ColumnCache> columnMap = new ConcurrentHashMap<>();


    @Override
    protected String columnToString(SFunction<T, ?> column) {
        ColumnCache cache = getColumnCache(column);
        return cache.getColumn();
    }

    /**
     * 获取 SerializedLambda 对应的列信息，从 lambda 表达式中推测实体类
     * <p>
     * 如果获取不到列信息，那么本次条件组装将会失败
     *
     * @param column 列
     * @return 列
     * @throws MilvusException 获取不到列信息时抛出异常
     */
    protected ColumnCache getColumnCache(SFunction<T, ?> column) {
        LambdaMeta meta = LambdaUtils.extract(column);
        String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
        String formatKey = LambdaUtils.formatKey(fieldName);
        if (columnMap.containsKey(formatKey)) {
            return columnMap.get(formatKey);
        }
        ColumnCache columnCache = new ColumnCache(fieldName);
        columnMap.put(formatKey, columnCache);
        return columnCache;
    }
}
