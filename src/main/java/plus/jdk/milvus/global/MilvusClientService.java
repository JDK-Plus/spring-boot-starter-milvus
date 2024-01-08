package plus.jdk.milvus.global;

import com.google.gson.Gson;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.*;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DropIndexParam;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.response.SearchResultsWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.util.ReflectionUtils;
import plus.jdk.milvus.annotation.VectorCollectionColumn;
import plus.jdk.milvus.annotation.VectorCollectionName;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.common.PropertyNamer;
import plus.jdk.milvus.conditions.query.QueryWrapper;
import plus.jdk.milvus.config.MilvusPlusProperties;
import plus.jdk.milvus.model.CollectionDefinition;
import plus.jdk.milvus.model.ColumnDefinition;
import plus.jdk.milvus.model.IIndexExtra;
import plus.jdk.milvus.model.Page;
import plus.jdk.milvus.record.VectorModel;
import plus.jdk.milvus.toolKit.CollectionUtils;
import plus.jdk.milvus.toolKit.LambdaUtils;
import plus.jdk.milvus.toolKit.support.LambdaMeta;
import plus.jdk.milvus.toolKit.support.SFunction;
import plus.jdk.milvus.wrapper.LambdaQueryWrapper;
import plus.jdk.milvus.wrapper.LambdaSearchWrapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MilvusClientService {

    private final MilvusPlusProperties properties;

    private final MilvusServiceClient milvusClient;

    private final BeanFactory beanFactory;

    private final Gson gson = new Gson();

    private final Map<Class<?>, CollectionDefinition> tableDefinitionMap = new ConcurrentHashMap<>();

    public MilvusClientService(MilvusPlusProperties properties, BeanFactory beanFactory) {
        ConnectParam.Builder builder = ConnectParam.newBuilder();
        if (properties.getHost() != null) {
            builder.withHost(properties.getHost());
        }
        if (properties.getPort() != null) {
            builder.withPort(properties.getPort());
        }
        if (properties.getUserName() != null) {
            builder.withAuthorization(properties.getUserName(), properties.getPassword());
        }
        if (properties.getConnectUri() != null) {
            builder.withUri(properties.getConnectUri());
        }
        if (properties.getConnectTimeout() != null) {
            builder.withConnectTimeout(properties.getConnectTimeout(), TimeUnit.MILLISECONDS);
        }
        if (properties.getRpcDeadline() != null) {
            builder.withRpcDeadline(properties.getRpcDeadline(), TimeUnit.MILLISECONDS);
        }
        if (properties.getDatabase() != null) {
            builder.withDatabaseName(properties.getDatabase());
        }
        if (properties.getSecure() != null) {
            builder.withSecure(properties.getSecure());
        }
        if (properties.getKeepAliveTime() != null) {
            builder.withKeepAliveTime(properties.getKeepAliveTime(), TimeUnit.MILLISECONDS);
        }
        if (properties.getIdleTimeout() != null) {
            builder.withIdleTimeout(properties.getIdleTimeout(), TimeUnit.MILLISECONDS);
        }
        if (properties.getToken() != null) {
            builder.withToken(properties.getToken());
        }
        this.properties = properties;
        this.beanFactory = beanFactory;
        this.milvusClient = new MilvusServiceClient(builder.build());
    }

    public String getColumnName(SFunction<?, ?> column, Class<?> clazz) throws MilvusException {
        LambdaMeta lambdaMeta = LambdaUtils.extract(column);
        String attributeName = PropertyNamer.methodToProperty(lambdaMeta.getImplMethodName());
        Field field;
        try {
            field = clazz.getDeclaredField(attributeName);
        } catch (Exception e) {
            throw new MilvusException(String.format("unknown attributeName '%s'", attributeName));
        }
        VectorCollectionColumn vectorCollectionColumn = field.getDeclaredAnnotation(VectorCollectionColumn.class);
        if (vectorCollectionColumn == null) {
            throw new MilvusException("table column must annotation with @VectorCollectionColumn");
        }
        return vectorCollectionColumn.name();
    }

    protected List<Field> getDeclaredFields(Class<?> clazz, List<Field> fields) {
        if (clazz == null) {
            return fields;
        }
        fields.addAll(Arrays.asList(this.getClass().getDeclaredFields()));
        getDeclaredFields(clazz.getSuperclass(), fields);
        return fields;
    }

    public <T extends VectorModel<?>> CollectionDefinition getTableDefinition(Class<T> clazz) throws MilvusException {
        if (tableDefinitionMap.containsKey(clazz)) {
            return tableDefinitionMap.get(clazz);
        }
        CollectionDefinition collectionDefinition = this.getCollectionDefinition(clazz);
        Field[] clazzFields = clazz.getDeclaredFields();
        for (Field field : clazzFields) {
            ReflectionUtils.makeAccessible(field);
            VectorCollectionColumn tableColumn = field.getDeclaredAnnotation(VectorCollectionColumn.class);
            if (tableColumn == null) {
                continue;
            }
            String columnName = tableColumn.name();
            VectorTypeHandler<?, ?> vectorTypeHandler = beanFactory.getBean(tableColumn.EmbeddingTypeHandler());
            ColumnDefinition columnDefinition = new ColumnDefinition();
            columnDefinition.setDesc(tableColumn.desc());
            columnDefinition.setName(columnName);
            columnDefinition.setPrimary(tableColumn.primary());
            columnDefinition.setDataType(tableColumn.dataType());
            columnDefinition.setVectorTypeHandler(vectorTypeHandler);
            columnDefinition.setField(field);
            columnDefinition.setPartitionKey(tableColumn.partitionKey());
            columnDefinition.setVectorDimension(tableColumn.vectorDimension());
            columnDefinition.setMaxLength(tableColumn.maxLength());
            columnDefinition.setIndex(tableColumn.index());
            columnDefinition.setIndexType(tableColumn.indexType());
            columnDefinition.setMetricType(tableColumn.metricType());
            collectionDefinition.getColumns().add(columnDefinition);
        }
        this.tableDefinitionMap.put(clazz, collectionDefinition);
        return collectionDefinition;
    }

    @NotNull
    private <T extends VectorModel<?>> CollectionDefinition getCollectionDefinition(Class<T> clazz) {
        CollectionDefinition collectionDefinition = new CollectionDefinition();
        VectorCollectionName vectorCollectionName = clazz.getAnnotation(VectorCollectionName.class);
        if (vectorCollectionName == null) {
            throw new MilvusException("table model must annotation with @VectorCollectionName");
        }
        collectionDefinition.setDescription(vectorCollectionName.description());
        collectionDefinition.setName(vectorCollectionName.name());
        collectionDefinition.setDatabase(vectorCollectionName.database());
        collectionDefinition.setClazz(clazz);
        return collectionDefinition;
    }

    public <T extends VectorModel<?>> boolean remove(Object pk, Class<T> clazz) throws MilvusException {
        CollectionDefinition collection = getTableDefinition(clazz);
        String columnName = collection.getPrimaryColumn().getName();
        String expression = new QueryWrapper<T>().in(columnName, pk).getExprSegment();
        if (StringUtils.isEmpty(expression)) {
            throw new MilvusException("expression is null");
        }
        DeleteParam.Builder builder = DeleteParam.newBuilder().withCollectionName(collection.getName()).withExpr(expression);
        R<MutationResult> resultR = milvusClient.delete(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        return true;
    }

    public <T extends VectorModel<?>> boolean batchRemove(LambdaQueryWrapper<T> wrapper) throws MilvusException {
        CollectionDefinition collection = getTableDefinition(wrapper.getEntityClass());
        String expression = wrapper.getExprSelect();
        if (StringUtils.isEmpty(expression)) {
            throw new MilvusException("expression is null");
        }
        DeleteParam.Builder builder = DeleteParam.newBuilder().withCollectionName(collection.getName()).withExpr(expression);
        R<MutationResult> resultR = milvusClient.delete(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        return true;
    }


    public <T extends VectorModel<?>> Boolean insert(T vectorModel) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(vectorModel.getClass());
        InsertParam.Builder builder = InsertParam.newBuilder();
        List<InsertParam.Field> dataFields = new ArrayList<>();
        for (ColumnDefinition columnDefinition : collectionDefinition.getColumns()) {
            ReflectionUtils.makeAccessible(columnDefinition.getField());
            String columnName = columnDefinition.getName();
            Object value = ReflectionUtils.getField(columnDefinition.getField(), vectorModel);
            if (value == null) {
                continue;
            }
            VectorTypeHandler vectorTypeHandler = columnDefinition.getVectorTypeHandler();
            List<?> dataVector = vectorTypeHandler.serialize(value);
            dataFields.add(new InsertParam.Field(columnName, dataVector));
        }
        if (!StringUtils.isEmpty(collectionDefinition.getDatabase())) {
            builder.withDatabaseName(collectionDefinition.getDatabase());
        }
        builder.withCollectionName(collectionDefinition.getName());
        builder.withFields(dataFields);
        InsertParam insertParam = builder.build();
        R<MutationResult> resultR = milvusClient.insert(insertParam);
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        if (resultR.getData() != null && !resultR.getData().getIDs().getIntId().getDataList().isEmpty()) {
            ColumnDefinition column = collectionDefinition.getPrimaryColumn();
            ReflectionUtils.makeAccessible(column.getField());
            Object id = resultR.getData().getIDs().getIntId().getDataList().get(0);
            ReflectionUtils.setField(column.getField(), vectorModel, id);
        }
        return true;
    }

    public <T extends VectorModel<?>> void loadCollection(Class<T> clazz) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        LoadCollectionParam.Builder builder = LoadCollectionParam.newBuilder();
        builder.withCollectionName(collectionDefinition.getName());
        if (!StringUtils.isEmpty(collectionDefinition.getDatabase())) {
            builder.withDatabaseName(collectionDefinition.getDatabase());
        }
        R<RpcStatus> resultR = milvusClient.loadCollection(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
    }

    public <T extends VectorModel<?>> void releaseCollection(Class<T> clazz) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        milvusClient.releaseCollection(ReleaseCollectionParam.newBuilder().withCollectionName(collectionDefinition.getName()).build());
    }

    public <T extends VectorModel<?>> void dropCollection(Class<T> clazz) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        DropCollectionParam.Builder builder = DropCollectionParam.newBuilder().withCollectionName(collectionDefinition.getName());
        if (!StringUtils.isEmpty(collectionDefinition.getDatabase())) {
            builder.withDatabaseName(collectionDefinition.getDatabase());
        }
        R<RpcStatus> resultR = milvusClient.dropCollection(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
    }

    public <T extends VectorModel<?>> boolean hasCollection(Class<T> clazz) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        HasCollectionParam.Builder builder = HasCollectionParam.newBuilder().withCollectionName(collectionDefinition.getName());
        if (!StringUtils.isEmpty(collectionDefinition.getDatabase())) {
            builder.withDatabaseName(collectionDefinition.getDatabase());
        }
        R<Boolean> resultR = milvusClient.hasCollection(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        return resultR.getData();
    }


    public <T extends VectorModel<?>> boolean dropIndex(Class<T> clazz, String indexName) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        DropIndexParam.Builder builder = DropIndexParam.newBuilder().withCollectionName(collectionDefinition.getName()).withIndexName(indexName);
        R<RpcStatus> resultR = milvusClient.dropIndex(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        return true;
    }

    public <T extends VectorModel<?>> LoadState getLoadState(Class<T> clazz) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        GetLoadStateParam.Builder builder = GetLoadStateParam.newBuilder();
        builder.withCollectionName(collectionDefinition.getName());
        if (!StringUtils.isEmpty(collectionDefinition.getDatabase())) {
            builder.withDatabaseName(collectionDefinition.getDatabase());
        }
        R<GetLoadStateResponse> resultR = milvusClient.getLoadState(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        return resultR.getData().getState();
    }

    public <T extends VectorModel<?>> Long getLoadProgress(Class<T> clazz) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        GetLoadingProgressParam.Builder builder = GetLoadingProgressParam.newBuilder();
        builder.withCollectionName(collectionDefinition.getName());
        R<GetLoadingProgressResponse> resultR = milvusClient.getLoadingProgress(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        return resultR.getData().getProgress();
    }

    public <T extends VectorModel<?>> boolean createIndex(Class<T> clazz, String indexName, SFunction<?, ?> column, IIndexExtra extra) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        CreateIndexParam.Builder builder = CreateIndexParam.newBuilder();
        builder.withCollectionName(collectionDefinition.getName());
        String columnName = getColumnName(column, clazz);
        ColumnDefinition columnDefinition = collectionDefinition.getColumnByColumnName(columnName);
        builder.withFieldName(columnName);
        builder.withIndexName(indexName);
        builder.withIndexType(columnDefinition.getIndexType());
        builder.withMetricType(columnDefinition.getMetricType());
        if (extra != null) {
            builder.withExtraParam(new Gson().toJson(extra));
        }
        builder.withSyncMode(Boolean.FALSE);
        R<RpcStatus> resultR = milvusClient.createIndex(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        return true;
    }

    public <T extends VectorModel<?>> boolean createCollection(Class<T> clazz) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        CreateCollectionParam.Builder builder = CreateCollectionParam.newBuilder();
        builder.withCollectionName(collectionDefinition.getName());
        builder.withDescription(collectionDefinition.getDescription());
        for (ColumnDefinition column : collectionDefinition.getColumns()) {
            FieldType.Builder fieldBuilder = FieldType.newBuilder();
            fieldBuilder.withName(column.getName());
            fieldBuilder.withDescription(column.getDesc());
            fieldBuilder.withPartitionKey(column.getPartitionKey());
            fieldBuilder.withDataType(column.getDataType());
            fieldBuilder.withPrimaryKey(column.getPrimary());
            if (column.vectorColumn()) {
                fieldBuilder.withDimension(column.getVectorDimension());
            }
            if (column.getDataType() == DataType.VarChar) {
                fieldBuilder.withMaxLength(column.getMaxLength());
            }
            if (column.getPrimary()) {
                fieldBuilder.withAutoID(true);
            }
            if (column.canBePartitionKey()) {
                fieldBuilder.withPartitionKey(column.getPartitionKey());
            }
            builder.addFieldType(fieldBuilder.build());
        }
        R<RpcStatus> resultR = milvusClient.createCollection(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        return true;
    }

    private <T extends VectorModel<?>> T createInstance(Class<T> clazz) throws MilvusException {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new MilvusException(e.getMessage());
        }
    }

    public <T extends VectorModel<?>> List<T> search(LambdaSearchWrapper<T> wrapper) throws MilvusException {
        return this.search(wrapper, wrapper.getEntityClass());
    }

    public <T extends VectorModel<?>> List<T> search(LambdaSearchWrapper<T> wrapper, Class<T> clazz) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        List<String> outFields = new ArrayList<>();
        for (ColumnDefinition columnDefinition : collectionDefinition.getColumns()) {
            if (columnDefinition.vectorColumn()) {
                continue;
            }
            outFields.add(columnDefinition.getName());
        }
        SearchParam.Builder builder = SearchParam.newBuilder();
        String vectorColumnName = getColumnName(wrapper.getVectorColumn(), clazz);
        ColumnDefinition columnDefinition = collectionDefinition.getColumnByColumnName(vectorColumnName);
        VectorTypeHandler vectorTypeHandler = columnDefinition.getVectorTypeHandler();
        List<?> vectors = vectorTypeHandler.serialize(wrapper.getVectorValue());
        builder.withVectors(vectors);
        builder.withVectorFieldName(columnDefinition.getName());
        builder.withCollectionName(collectionDefinition.getName());
        builder.withConsistencyLevel(wrapper.getConsistencyLevel());
        builder.withMetricType(columnDefinition.getMetricType());
        builder.withOutFields(outFields);
        builder.withTopK(wrapper.getTopK());
        String expression = wrapper.getExprSelect();
        if (!StringUtils.isEmpty(expression)) {
            builder.withExpr(expression);
        }
        if (!CollectionUtils.isEmpty(wrapper.getPartitionNames())) {
            builder.withPartitionNames(wrapper.getPartitionNames());
        }
        if (wrapper.getExtra() != null) {
            builder.withParams(gson.toJson(wrapper.getExtra()));
        }
        R<SearchResults> resultR = milvusClient.search(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        SearchResultsWrapper resultsWrapper = new SearchResultsWrapper(resultR.getData().getResults());
        List<T> resultRows = new ArrayList<>();
        for (int i = 0; i < resultsWrapper.getRowRecords().size(); i++) {
            QueryResultsWrapper.RowRecord rowRecord = resultsWrapper.getRowRecords().get(i);
            T data = this.createInstance(clazz);
            Object distance = rowRecord.get("distance");
            if (distance instanceof Float) {
                data.setDistance((Float) rowRecord.get("distance"));
            }
            for (String columnName : rowRecord.getFieldValues().keySet()) {
                ColumnDefinition column = collectionDefinition.getColumnByColumnName(columnName);
                if (column == null) {
                    continue;
                }
                ReflectionUtils.makeAccessible(column.getField());
                ReflectionUtils.setField(column.getField(), data, rowRecord.get(columnName));
            }
            resultRows.add(data);
        }
        return resultRows;
    }

    public <T extends VectorModel<?>> List<T> query(LambdaQueryWrapper<T> wrapper) throws MilvusException {
        Class<T> clazz = wrapper.getEntityClass();
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        List<String> outFields = new ArrayList<>();
        for (ColumnDefinition columnDefinition : collectionDefinition.getColumns()) {
            if (columnDefinition.vectorColumn()) {
                continue;
            }
            outFields.add(columnDefinition.getName());
        }
        QueryParam.Builder builder = QueryParam.newBuilder();
        if (!CollectionUtils.isEmpty(wrapper.getPartitionNames())) {
            builder.withPartitionNames(wrapper.getPartitionNames());
        }
        if (!CollectionUtils.isEmpty(wrapper.getPartitionNames())) {
            builder.withPartitionNames(wrapper.getPartitionNames());
        }
        builder.withCollectionName(collectionDefinition.getName());
        builder.withConsistencyLevel(wrapper.getConsistencyLevel());
        builder.withOutFields(outFields);
        if (wrapper.getLimit() != null) {
            builder.withLimit(wrapper.getLimit());
        }
        if (wrapper.getOffset() != null) {
            builder.withOffset(wrapper.getOffset());
        }
        String expression = wrapper.getExprSelect();
        if (!StringUtils.isEmpty(expression)) {
            builder.withExpr(expression);
        }
        R<QueryResults> resultR = milvusClient.query(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        QueryResultsWrapper resultsWrapper = new QueryResultsWrapper(resultR.getData());
        List<T> resultRows = new ArrayList<>();
        for (int i = 0; i < resultsWrapper.getRowRecords().size(); i++) {
            QueryResultsWrapper.RowRecord rowRecord = resultsWrapper.getRowRecords().get(i);
            T data = this.createInstance(clazz);
            rowRecord.getFieldValues().keySet().forEach(columnName -> {
                ColumnDefinition column = collectionDefinition.getColumnByColumnName(columnName);
                if (column == null) {
                    return;
                }
                ReflectionUtils.makeAccessible(column.getField());
                ReflectionUtils.setField(column.getField(), data, rowRecord.get(columnName));
            });
            resultRows.add(data);
        }
        return resultRows;
    }

    public <T extends VectorModel<?>> GetCollectionStatisticsResponse getCollectionStatistics(Class<T> clazz) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        GetCollectionStatisticsParam.Builder builder = GetCollectionStatisticsParam.newBuilder();
        builder.withCollectionName(collectionDefinition.getName());
        if (!StringUtils.isEmpty(collectionDefinition.getDatabase())) {
            builder.withDatabaseName(collectionDefinition.getDatabase());
        }
        R<GetCollectionStatisticsResponse> resultR = milvusClient.getCollectionStatistics(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        return resultR.getData();
    }

    public <T extends VectorModel<?>> Page<T> queryPage(LambdaQueryWrapper<T> wrapper, Long page, Long pageSize) throws MilvusException {
        Page<T> dataPage = new Page<>();
        dataPage.setPage(page);
        dataPage.setPageSize(pageSize);
        wrapper.setLimit(pageSize);
        wrapper.setOffset(page * pageSize);
        List<T> instanceList = this.query(wrapper);
        dataPage.setInstances(instanceList);
        return dataPage;
    }

    public <T extends VectorModel<?>> Long getRowCount(Class<T> clazz) throws MilvusException {
        GetCollectionStatisticsResponse statistics = getCollectionStatistics(clazz);
        for (KeyValuePair pair : statistics.getStatsList()) {
            if ("row_count".equals(pair.getKey())) {
                return Long.parseLong(pair.getValue());
            }
        }
        return 0L;
    }
}
