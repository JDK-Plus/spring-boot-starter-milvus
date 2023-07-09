package plus.jdk.milvus.global;

import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.google.gson.Gson;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.grpc.*;
import io.milvus.param.*;
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
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import io.milvus.client.MilvusServiceClient;
import org.springframework.util.ReflectionUtils;
import plus.jdk.cli.common.StringUtils;
import plus.jdk.milvus.annotation.VectorCollectionColumn;
import plus.jdk.milvus.annotation.VectorCollectionName;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.common.Operator;
import plus.jdk.milvus.config.MilvusPlusProperties;
import plus.jdk.milvus.model.ColumnDefinition;
import plus.jdk.milvus.model.CollectionDefinition;
import plus.jdk.milvus.model.IIndexExtra;
import plus.jdk.milvus.model.WrapperModel;
import plus.jdk.milvus.record.VectorModel;
import plus.jdk.milvus.wrapper.LambdaQueryWrapper;
import plus.jdk.milvus.wrapper.LambdaSearchWrapper;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class MilvusClientService {

    private final MilvusPlusProperties properties;

    private final MilvusServiceClient milvusClient;

    private final BeanFactory beanFactory;

    private final ApplicationContext applicationContext;

    private final Gson gson = new Gson();

    private final Map<Class<?>, CollectionDefinition> tableDefinitionMap = new ConcurrentHashMap<>();

    public String getColumnName(SFunction<?, ?> column, Class<?> clazz) throws MilvusException {
        LambdaMeta lambdaMeta = LambdaUtils.extract(column);
        String attributeName = PropertyNamer.methodToProperty(lambdaMeta.getImplMethodName());
        Field field = null;
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

    public MilvusClientService(MilvusPlusProperties properties,
                               BeanFactory beanFactory,
                               ApplicationContext applicationContext) {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(properties.getHost())
                .withPort(properties.getPort())
                .withAuthorization(properties.getUserName(), properties.getPassword())
                .build();
        this.properties = properties;
        this.beanFactory = beanFactory;
        this.applicationContext = applicationContext;
        this.milvusClient = new MilvusServiceClient(connectParam);
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
        CollectionDefinition collectionDefinition = new CollectionDefinition();
        VectorCollectionName vectorCollectionName = clazz.getAnnotation(VectorCollectionName.class);
        if (vectorCollectionName == null) {
            throw new MilvusException("table model must annotation with @VectorCollectionName");
        }
        collectionDefinition.setDescription(vectorCollectionName.description());
        collectionDefinition.setName(vectorCollectionName.name());
        collectionDefinition.setDatabase(vectorCollectionName.database());
        collectionDefinition.setClazz(clazz);
        Field[] clazzFields = clazz.getDeclaredFields();
        for (Field field : clazzFields) {
            ReflectionUtils.makeAccessible(field);
            VectorCollectionColumn tableColumn = field.getDeclaredAnnotation(VectorCollectionColumn.class);
            if (tableColumn == null) {
                throw new MilvusException("table column must annotation with @VectorCollectionColumn");
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

    public <T extends VectorModel<?>> boolean remove(Object pk, Class<T> clazz) throws MilvusException {
        CollectionDefinition collection = getTableDefinition(clazz);
        String columnName = collection.getPrimaryColumn().getName();
        String expression = Operator.in.getIOperatorComputer().compute(columnName, new Object[]{pk}, clazz);
        if(StringUtils.isEmpty(expression)) {
            throw new MilvusException("expression is null");
        }
        DeleteParam.Builder builder = DeleteParam.newBuilder()
                .withCollectionName(collection.getName())
                .withExpr(expression);
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
        if (resultR.getData() != null && resultR.getData().getIDs().getIntId().getDataList().size() > 0) {
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
        milvusClient.releaseCollection(
                ReleaseCollectionParam.newBuilder()
                        .withCollectionName(collectionDefinition.getName())
                        .build()
        );
    }

    public <T extends VectorModel<?>> void dropCollection(Class<T> clazz) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        DropCollectionParam.Builder builder = DropCollectionParam.newBuilder()
                .withCollectionName(collectionDefinition.getName());
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
        HasCollectionParam.Builder builder = HasCollectionParam.newBuilder()
                .withCollectionName(collectionDefinition.getName());
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
        DropIndexParam.Builder builder = DropIndexParam.newBuilder()
                .withCollectionName(collectionDefinition.getName())
                .withIndexName(indexName);
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

    public <T extends VectorModel<?>> boolean createIndex(Class<T> clazz, String indexName,
                                                          SFunction<?, ?> column, IIndexExtra extra) throws MilvusException {
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

    private  <T extends VectorModel<?>> T createInstance(Class<T> clazz) throws MilvusException {
        try{
            return clazz.newInstance();
        }catch (Exception e) {
            throw new MilvusException(e.getMessage());
        }
    }

    public <T extends VectorModel<?>> List<T> search(LambdaSearchWrapper<T> wrapper, Class<T> clazz) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        List<String> outFields = new ArrayList<>();
        for (ColumnDefinition columnDefinition : collectionDefinition.getColumns()) {
            if(columnDefinition.vectorColumn()) {
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
        String expression = wrapper.buildExpression(clazz);
        if(!StringUtils.isEmpty(expression)) {
            builder.withExpr(expression);
        }
        if(wrapper.getExtra() != null) {
            builder.withParams(gson.toJson(wrapper.getExtra()));
        }
        R<SearchResults> resultR = milvusClient.search(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        SearchResultsWrapper resultsWrapper = new SearchResultsWrapper(resultR.getData().getResults());
        List<T> resultRows = new ArrayList<>();
        for(int i = 0; i < resultsWrapper.getRowRecords().size(); i ++) {
            QueryResultsWrapper.RowRecord rowRecord = resultsWrapper.getRowRecords().get(i);
            T data = this.createInstance(clazz);
            for(String columnName:rowRecord.getFieldValues().keySet()) {
                ColumnDefinition column = collectionDefinition.getColumnByColumnName(columnName);
                if(column == null) {
                    continue; // TODO: 此处需要后续再处理一下子分值和distance相关逻辑，看能不能按要求写回去
                }
                ReflectionUtils.makeAccessible(column.getField());
                ReflectionUtils.setField(column.getField(), data, rowRecord.get(columnName));
            }
            resultRows.add(data);
        }
        return resultRows;
    }


    public <T extends VectorModel<?>> List<T> query(LambdaQueryWrapper<T> wrapper, Class<T> clazz) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        List<String> outFields = new ArrayList<>();
        for (ColumnDefinition columnDefinition : collectionDefinition.getColumns()) {
            if(columnDefinition.vectorColumn()) {
                continue;
            }
            outFields.add(columnDefinition.getName());
        }
        QueryParam.Builder builder = QueryParam.newBuilder();
        builder.withCollectionName(collectionDefinition.getName());
        builder.withConsistencyLevel(wrapper.getConsistencyLevel());
        builder.withOutFields(outFields);
        if(wrapper.getLimit() != null) {
            builder.withLimit(wrapper.getLimit());
        }
        if(wrapper.getOffset() != null) {
            builder.withOffset(wrapper.getOffset());
        }
        String expression = wrapper.buildExpression(clazz);
        if(!StringUtils.isEmpty(expression)) {
            builder.withExpr(expression);
        }
        R<QueryResults> resultR = milvusClient.query(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        QueryResultsWrapper resultsWrapper = new QueryResultsWrapper(resultR.getData());
        List<T> resultRows = new ArrayList<>();
        for(int i = 0; i < resultsWrapper.getRowRecords().size(); i ++) {
            QueryResultsWrapper.RowRecord rowRecord = resultsWrapper.getRowRecords().get(i);
            T data = this.createInstance(clazz);
            for(String columnName:rowRecord.getFieldValues().keySet()) {
                ColumnDefinition column = collectionDefinition.getColumnByColumnName(columnName);
                if(column == null) {
                    continue;
                }
                ReflectionUtils.makeAccessible(column.getField());
                ReflectionUtils.setField(column.getField(), data, rowRecord.get(columnName));
            }
            resultRows.add(data);
        }
        return resultRows;
    }


}
