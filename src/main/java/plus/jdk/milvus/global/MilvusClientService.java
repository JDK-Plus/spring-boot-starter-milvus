package plus.jdk.milvus.global;

import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.SearchResults;
import io.milvus.param.IndexType;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import org.springframework.util.ReflectionUtils;
import plus.jdk.cli.common.StringUtils;
import plus.jdk.milvus.annotation.VectorTableColumn;
import plus.jdk.milvus.annotation.VectorTableName;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.config.MilvusPlusProperties;
import plus.jdk.milvus.model.TableColumnDefinition;
import plus.jdk.milvus.model.TableDefinition;
import plus.jdk.milvus.record.VectorModel;

import javax.naming.directory.SearchResult;
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

    private final Map<Class<?>, TableDefinition> tableDefinitionMap = new ConcurrentHashMap<>();

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

    private List<List<Float>> generateFloatVectors(int count) {
        Random ran = new Random();
        int VECTOR_DIM = 64;
        List<List<Float>> vectors = new ArrayList<>();
        for (int n = 0; n < count; ++n) {
            List<Float> vector = new ArrayList<>();
            for (int i = 0; i < VECTOR_DIM; ++i) {
                vector.add(ran.nextFloat());
            }
            vectors.add(vector);
        }
        return vectors;
    }

    protected List<Field> getDeclaredFields(Class<?> clazz, List<Field> fields) {
        if (clazz == null) {
            return fields;
        }
        fields.addAll(Arrays.asList(this.getClass().getDeclaredFields()));
        getDeclaredFields(clazz.getSuperclass(), fields);
        return fields;
    }

    protected <T extends VectorModel<?>> TableDefinition getTableDefinition(T vectorModel) throws MilvusException {
        Class<?> clazz = vectorModel.getClass();
        if (tableDefinitionMap.containsKey(clazz)) {
            return tableDefinitionMap.get(clazz);
        }
        TableDefinition tableDefinition = new TableDefinition();
        VectorTableName vectorTableName = clazz.getAnnotation(VectorTableName.class);
        if (vectorTableName == null) {
            throw new MilvusException("table model must annotation with @VectorTableName");
        }
        tableDefinition.setDescription(vectorTableName.description());
        tableDefinition.setName(vectorTableName.name());
        tableDefinition.setDatabase(vectorTableName.database());
        tableDefinition.setClazz(clazz);
        List<Field> clazzFields = getDeclaredFields(clazz, new ArrayList<>());
        for (Field field : clazzFields) {
            ReflectionUtils.makeAccessible(field);
            VectorTableColumn tableColumn = field.getDeclaredAnnotation(VectorTableColumn.class);
            if (tableColumn == null) {
                throw new MilvusException("table column must annotation with @VectorTableField");
            }
            String columnName = tableColumn.name();
            EmbeddingTypeHandler<?, ?> embeddingTypeHandler = beanFactory.getBean(tableColumn.EmbeddingTypeHandler());
            TableColumnDefinition columnDefinition = new TableColumnDefinition();
            columnDefinition.setDesc(tableColumn.desc());
            columnDefinition.setName(columnName);
            columnDefinition.setDataType(tableColumn.dataType());
            columnDefinition.setEmbeddingTypeHandler(tableColumn.EmbeddingTypeHandler());
            columnDefinition.setField(field);
            tableDefinition.getColumns().add(columnDefinition);
        }
        this.tableDefinitionMap.put(clazz, tableDefinition);
        return tableDefinition;
    }

    public R<SearchResults> search(VectorModel<?> object) throws MilvusException {
        TableDefinition tableDefinition = getTableDefinition(object);
        SearchParam.Builder builder = SearchParam.newBuilder();
        return milvusClient.search(builder.build());
    }


    public <T extends VectorModel<?>> R<MutationResult> insert(T vectorModel) throws MilvusException {
        TableDefinition tableDefinition = getTableDefinition(vectorModel);
        InsertParam.Builder builder = InsertParam.newBuilder();
        List<InsertParam.Field> dataFields = new ArrayList<>();
        for (TableColumnDefinition columnDefinition : tableDefinition.getColumns()) {
            ReflectionUtils.makeAccessible(columnDefinition.getField());
            String columnName = columnDefinition.getName();;
            Object value = ReflectionUtils.getField(columnDefinition.getField(), vectorModel);
            EmbeddingTypeHandler embeddingTypeHandler = beanFactory.getBean(columnDefinition.getEmbeddingTypeHandler());
            List<?> dataVector = embeddingTypeHandler.computeDataVector(value);
            dataFields.add(new InsertParam.Field(columnName, dataVector));
        }
        if (!StringUtils.isEmpty(tableDefinition.getDatabase())) {
            builder.withDatabaseName(tableDefinition.getDatabase());
        }
        builder.withCollectionName(tableDefinition.getName());
        builder.withFields(dataFields);
        InsertParam insertParam = builder.build();
        return milvusClient.insert(insertParam);
    }


    public static void main(String[] args) {

    }
}
