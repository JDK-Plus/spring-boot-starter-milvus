package plus.jdk.milvus.conditions;

import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.common.PropertyNamer;
import plus.jdk.milvus.metadata.CollectionHelper;
import plus.jdk.milvus.record.VectorModel;
import plus.jdk.milvus.toolkit.Assert;
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
        Class<?> instantiatedClass = meta.getInstantiatedClass();
        tryInitCache(instantiatedClass);
        return getColumnCache(fieldName, instantiatedClass);
    }

    private void tryInitCache(Class<?> lambdaClass) {
        final Class<T> entityClass = getEntityClass();
        if (entityClass != null) {
            lambdaClass = entityClass;
        }
        Map<String, ColumnCache> cacheMap = LambdaUtils.getColumnMap(lambdaClass);
        if (cacheMap == null) {
            CollectionHelper.initCollectionInfo(lambdaClass);
            cacheMap = LambdaUtils.getColumnMap(lambdaClass);
        }
        columnMap.putAll(cacheMap);
    }

    private ColumnCache getColumnCache(String fieldName, Class<?> lambdaClass) {
        ColumnCache columnCache = columnMap.get(LambdaUtils.formatKey(fieldName));
        Assert.notNull(columnCache, "can not find lambda cache for this property [%s] of entity [%s]",
                fieldName, lambdaClass.getName());
        return columnCache;
    }
}
