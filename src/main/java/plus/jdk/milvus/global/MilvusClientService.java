package plus.jdk.milvus.global;

import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DropIndexParam;
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
import plus.jdk.milvus.config.MilvusPlusProperties;
import plus.jdk.milvus.model.CollectionColumnDefinition;
import plus.jdk.milvus.model.CollectionDefinition;
import plus.jdk.milvus.record.VectorModel;

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

    private final Map<Class<?>, CollectionDefinition> tableDefinitionMap = new ConcurrentHashMap<>();

    private String getColumnName(SFunction<?, ?> column) {
        LambdaMeta lambdaMeta = LambdaUtils.extract(column);
        return PropertyNamer.methodToProperty(lambdaMeta.getImplMethodName());
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
            throw new MilvusException("table model must annotation with @VectorTableName");
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
                throw new MilvusException("table column must annotation with @VectorTableField");
            }
            String columnName = tableColumn.name();
            VectorTypeHandler<?, ?> vectorTypeHandler = beanFactory.getBean(tableColumn.EmbeddingTypeHandler());
            CollectionColumnDefinition columnDefinition = new CollectionColumnDefinition();
            columnDefinition.setDesc(tableColumn.desc());
            columnDefinition.setName(columnName);
            columnDefinition.setPrimary(tableColumn.primary());
            columnDefinition.setDataType(tableColumn.dataType());
            columnDefinition.setVectorTypeHandler(vectorTypeHandler);
            columnDefinition.setField(field);
            columnDefinition.setVectorDimension(tableColumn.vectorDimension());
            columnDefinition.setMaxLength(tableColumn.maxLength());
            collectionDefinition.getColumns().add(columnDefinition);
        }
        this.tableDefinitionMap.put(clazz, collectionDefinition);
        return collectionDefinition;
    }

    public R<SearchResults> search(VectorModel<?> object) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(object.getClass());
        SearchParam.Builder builder = SearchParam.newBuilder();
        return milvusClient.search(builder.build());
    }


    public <T extends VectorModel<?>> Boolean insert(T vectorModel) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(vectorModel.getClass());
        InsertParam.Builder builder = InsertParam.newBuilder();
        List<InsertParam.Field> dataFields = new ArrayList<>();
        for (CollectionColumnDefinition columnDefinition : collectionDefinition.getColumns()) {
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
            CollectionColumnDefinition column = collectionDefinition.getPrimaryColumn();
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
        if(!StringUtils.isEmpty(collectionDefinition.getDatabase())) {
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
        if(!StringUtils.isEmpty(collectionDefinition.getDatabase())) {
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

    public <T extends VectorModel<?>> boolean createIndex(Class<T> clazz, String indexName, SFunction<?, ?> column,
                                                          IndexType indexType, MetricType metricType, String extraParam) throws MilvusException {
        CollectionDefinition collectionDefinition = getTableDefinition(clazz);
        CreateIndexParam.Builder builder = CreateIndexParam.newBuilder();
        builder.withCollectionName(collectionDefinition.getName());
        String columnName = getColumnName(column);
        builder.withFieldName(columnName);
        builder.withIndexName(indexName);
        builder.withIndexType(indexType);
        builder.withMetricType(MetricType.L2);
        if(extraParam != null) {
            builder.withExtraParam(extraParam);
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
        for (CollectionColumnDefinition column : collectionDefinition.getColumns()) {
            FieldType.Builder fieldBuilder = FieldType.newBuilder();
            fieldBuilder.withName(column.getName());
            fieldBuilder.withDescription(column.getDesc());
            fieldBuilder.withDataType(column.getDataType());
            fieldBuilder.withPrimaryKey(column.getPrimary());
            if (column.getDataType() == DataType.BinaryVector || column.getDataType() == DataType.FloatVector) {
                fieldBuilder.withDimension(column.getVectorDimension());
            }
            if (column.getDataType() == DataType.VarChar) {
                fieldBuilder.withMaxLength(column.getMaxLength());
            }
            if (column.getPrimary()) {
                fieldBuilder.withAutoID(true);
            }
            builder.addFieldType(fieldBuilder.build());
        }
        R<RpcStatus> resultR = milvusClient.createCollection(builder.build());
        if (resultR.getStatus() != R.Status.Success.getCode() || resultR.getException() != null) {
            throw new MilvusException(resultR.getException().getMessage());
        }
        return true;
    }
}
