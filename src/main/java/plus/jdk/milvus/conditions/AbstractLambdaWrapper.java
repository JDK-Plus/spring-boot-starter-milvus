package plus.jdk.milvus.conditions;

import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.common.PropertyNamer;
import plus.jdk.milvus.record.VectorModel;
import plus.jdk.milvus.toolKit.LambdaUtils;
import plus.jdk.milvus.toolKit.support.ColumnCache;
import plus.jdk.milvus.toolKit.support.LambdaMeta;
import plus.jdk.milvus.toolKit.support.SFunction;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractLambdaWrapper<T extends VectorModel<? extends VectorModel<?>>, Children extends AbstractLambdaWrapper<T, Children>>
        extends AbstractWrapper<T, SFunction<T, ?>, Children> {

    private final Map<String, ColumnCache> columnMap = new ConcurrentHashMap<>();
//    private boolean initColumnMap = false;


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
     * @return 列
     * @throws MilvusException 获取不到列信息时抛出异常
     */
    protected ColumnCache getColumnCache(SFunction<T, ?> column) {
        LambdaMeta meta = LambdaUtils.extract(column);
        String fieldName = PropertyNamer.methodToProperty(meta.getImplMethodName());
//        Class<?> instantiatedClass = meta.getInstantiatedClass();
//        tryInitCache(instantiatedClass);
        String formatKey = LambdaUtils.formatKey(fieldName);
        if (columnMap.containsKey(formatKey)) {
            return columnMap.get(formatKey);
        }
        ColumnCache columnCache = new ColumnCache(fieldName);
        columnMap.put(formatKey, columnCache);
        return columnCache;
    }

//    private void tryInitCache(Class<?> lambdaClass) {
//        if (!initColumnMap) {
//            final Class<T> entityClass = getEntityClass();
//            if (entityClass != null) {
//                lambdaClass = entityClass;
//            }
//            columnMap = LambdaUtils.getColumnMap(lambdaClass);
//            Assert.notNull(columnMap, "can not find lambda cache for this entity [%s]", lambdaClass.getName());
//            initColumnMap = true;
//        }
//    }

//    private ColumnCache getColumnCache(String fieldName, Class<?> lambdaClass) {
//        ColumnCache columnCache = columnMap.get(LambdaUtils.formatKey(fieldName));
//        Assert.notNull(columnCache, "can not find lambda cache for this property [%s] of entity [%s]",
//                fieldName, lambdaClass.getName());
//        return columnCache;
//    }
}
