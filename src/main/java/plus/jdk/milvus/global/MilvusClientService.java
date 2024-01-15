package plus.jdk.milvus.global;

import com.google.gson.Gson;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
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
import org.springframework.util.ReflectionUtils;
import plus.jdk.milvus.annotation.VectorCollectionColumn;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.common.PropertyNamer;
import plus.jdk.milvus.conditions.query.QueryWrapper;
import plus.jdk.milvus.metadata.CollectionDefinition;
import plus.jdk.milvus.metadata.CollectionHelper;
import plus.jdk.milvus.metadata.ColumnDefinition;
import plus.jdk.milvus.model.IIndexExtra;
import plus.jdk.milvus.model.Page;
import plus.jdk.milvus.record.VectorModel;
import plus.jdk.milvus.toolkit.CollectionUtils;
import plus.jdk.milvus.toolkit.LambdaUtils;
import plus.jdk.milvus.toolkit.support.LambdaMeta;
import plus.jdk.milvus.toolkit.support.SFunction;
import plus.jdk.milvus.wrapper.LambdaQueryWrapper;
import plus.jdk.milvus.wrapper.LambdaSearchWrapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MilvusClientService {


    private final MilvusServiceClient milvusClient;

    private final Gson gson = new Gson();

    public MilvusClientService(MilvusServiceClient milvusClient) {
        this.milvusClient = milvusClient;
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

    public <T extends VectorModel<?>> boolean remove(Object pk, Class<T> clazz) throws MilvusException {
        CollectionDefinition collection = CollectionHelper.getCollectionInfo(clazz);
        String columnName = collection.getPrimaryColumn().getName();
        String expression = new QueryWrapper<T>().in(columnName, pk).getExprSegment();
        return remove(collection, expression);
    }

    private boolean remove(CollectionDefinition collection, String expression) {
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
        CollectionDefinition collection = CollectionHelper.getCollectionInfo(wrapper.getEntityClass());
        String expression = wrapper.getExprSelect();
        return remove(collection, expression);
    }


    public <T extends VectorModel<?>> Boolean insert(T vectorModel) throws MilvusException {
        CollectionDefinition collectionDefinition = CollectionHelper.getCollectionInfo(vectorModel.getClass());
        InsertParam.Builder builder = InsertParam.newBuilder();
        List<InsertParam.Field> dataFields = new ArrayList<>();
        for (ColumnDefinition columnDefinition : collectionDefinition.getColumns()) {
            ReflectionUtils.makeAccessible(columnDefinition.getField());
            String columnName = columnDefinition.getName();
            Object value = ReflectionUtils.getField(columnDefinition.getField(), vectorModel);
            if (value == null) {
                continue;
            }
            VectorTypeHandler<Object> vectorTypeHandler = columnDefinition.getVectorTypeHandler();
            List<Object> dataVector = vectorTypeHandler.serialize(value);
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
        CollectionDefinition collectionDefinition = CollectionHelper.getCollectionInfo(clazz);
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
        CollectionDefinition collectionDefinition = CollectionHelper.getCollectionInfo(clazz);
        milvusClient.releaseCollection(ReleaseCollectionParam.newBuilder().withCollectionName(collectionDefinition.getName()).build());
    }

    public <T extends VectorModel<?>> void dropCollection(Class<T> clazz) throws MilvusException {
        CollectionDefinition collectionDefinition = CollectionHelper.getCollectionInfo(clazz);
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
        CollectionDefinition collectionDefinition = CollectionHelper.getCollectionInfo(clazz);
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
        CollectionDefinition collectionDefinition = CollectionHelper.getCollectionInfo(clazz);
        DropIndexParam.Builder builder = DropIndexParam.newBuilder().withCollectionName(collectionDefinition.getName()).withIndexName(indexName);
        R<RpcStatus> resultR = milvusClient.dropIndex(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        return true;
    }

    public <T extends VectorModel<?>> LoadState getLoadState(Class<T> clazz) throws MilvusException {
        CollectionDefinition collectionDefinition = CollectionHelper.getCollectionInfo(clazz);
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
        CollectionDefinition collectionDefinition = CollectionHelper.getCollectionInfo(clazz);
        GetLoadingProgressParam.Builder builder = GetLoadingProgressParam.newBuilder();
        builder.withCollectionName(collectionDefinition.getName());
        R<GetLoadingProgressResponse> resultR = milvusClient.getLoadingProgress(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        return resultR.getData().getProgress();
    }

    public <T extends VectorModel<?>> boolean createIndex(Class<T> clazz, String indexName, SFunction<?, ?> column, IIndexExtra extra) throws MilvusException {
        CollectionDefinition collectionDefinition = CollectionHelper.getCollectionInfo(clazz);
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
        CollectionDefinition collectionDefinition = CollectionHelper.getCollectionInfo(clazz);
        CreateCollectionParam.Builder builder = CreateCollectionParam.newBuilder();
        builder.withCollectionName(collectionDefinition.getName());
        builder.withDescription(collectionDefinition.getDescription());
        for (ColumnDefinition column : collectionDefinition.getColumns()) {
            FieldType.Builder fieldBuilder = FieldType.newBuilder();
            fieldBuilder.withName(column.getName());
            fieldBuilder.withDescription(column.getDesc());
            fieldBuilder.withPartitionKey(column.getPartitionKey());
            fieldBuilder.withDataType(column.getDataType());
            Boolean primary = column.getPrimary();
            fieldBuilder.withPrimaryKey(primary);
            if (column.vectorColumn()) {
                fieldBuilder.withDimension(column.getVectorDimension());
            }
            if (column.getDataType() == DataType.VarChar) {
                fieldBuilder.withMaxLength(column.getMaxLength());
            }
            if (column.getDataType() == DataType.Array) {
                if (column.getElementType() == DataType.None) {
                    throw new MilvusException(column.getName() + " column must set elementType");
                }
                fieldBuilder.withElementType(column.getElementType());
                fieldBuilder.withMaxCapacity(column.getMaxCapacity());
                fieldBuilder.withMaxLength(column.getMaxLength());
            }
            if (Boolean.TRUE.equals(primary)) {
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
        CollectionDefinition collectionDefinition = CollectionHelper.getCollectionInfo(clazz);
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
        VectorTypeHandler<Object> vectorTypeHandler = columnDefinition.getVectorTypeHandler();
        List<?> vectors = vectorTypeHandler.serialize(wrapper.getVectorValue());
        builder.withVectors(vectors);
        builder.withVectorFieldName(columnDefinition.getName());
        builder.withCollectionName(collectionDefinition.getName());
        builder.withConsistencyLevel(wrapper.getConsistencyLevel());
        builder.withMetricType(columnDefinition.getMetricType());
        builder.withOutFields(outFields);
        builder.withTopK(wrapper.getTopK());
        String expression = wrapper.getExprSegment();
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
        CollectionDefinition collectionDefinition = CollectionHelper.getCollectionInfo(clazz);
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
        CollectionDefinition collectionDefinition = CollectionHelper.getCollectionInfo(clazz);
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
