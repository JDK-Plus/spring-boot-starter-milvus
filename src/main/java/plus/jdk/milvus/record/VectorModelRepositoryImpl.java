package plus.jdk.milvus.record;

import io.milvus.grpc.LoadState;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.global.MilvusClientService;
import plus.jdk.milvus.model.IIndexExtra;
import plus.jdk.milvus.model.Page;
import plus.jdk.milvus.selector.MilvusSelector;
import plus.jdk.milvus.toolkit.support.SFunction;
import plus.jdk.milvus.wrapper.LambdaQueryWrapper;
import plus.jdk.milvus.wrapper.LambdaSearchWrapper;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public abstract class VectorModelRepositoryImpl<T extends VectorModel<?>>
        implements VectorModelRepository<T>, Serializable {

    protected final Class<T> entityType;
    protected MilvusClientService milvusClientService;

    @SuppressWarnings("unchecked")
    protected VectorModelRepositoryImpl() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) superClass;
            entityType = (Class<T>) parameterizedType.getActualTypeArguments()[0];
        } else {
            throw new IllegalArgumentException("Unable to determine the entity type.");
        }
    }

    public boolean insert(T vectorModel) throws MilvusException {
        return getMilvusClientService().insert(vectorModel);
    }

    public boolean remove(Object pk) throws MilvusException {
        return getMilvusClientService().remove(pk, entityType);
    }

    public boolean batchRemove(LambdaQueryWrapper<T> wrapper) throws MilvusException {
        wrapper.setEntityClass(entityType);
        return getMilvusClientService().batchRemove(wrapper);
    }

    public boolean createCollection() throws MilvusException {
        return getMilvusClientService().createCollection(entityType);
    }

    public void loadCollection() throws MilvusException {
        getMilvusClientService().loadCollection(entityType);
    }

    public LoadState getLoadState() throws MilvusException {
        return getMilvusClientService().getLoadState(entityType);
    }

    public Long getLoadProgress() throws MilvusException {
        return getMilvusClientService().getLoadProgress(entityType);
    }


    public void releaseCollection() throws MilvusException {
        getMilvusClientService().releaseCollection(entityType);
    }

    public boolean createIndex(String indexName, SFunction<T, ?> column, IIndexExtra extraParam) throws MilvusException {
        return getMilvusClientService().createIndex(entityType, indexName, column, extraParam);
    }

    public boolean dropIndex(String indexName) throws MilvusException {
        return getMilvusClientService().dropIndex(entityType, indexName);
    }

    public void dropCollection() throws MilvusException {
        getMilvusClientService().dropCollection(entityType);
    }

    public boolean hasCollection() throws MilvusException {
        return getMilvusClientService().hasCollection(entityType);
    }

    public List<T> search(LambdaSearchWrapper<T> wrapper) throws MilvusException {
        wrapper.setEntityClass(entityType);
        return getMilvusClientService().search(wrapper);
    }

    public List<T> query(LambdaQueryWrapper<T> wrapper) throws MilvusException {
        wrapper.setEntityClass(entityType);
        return getMilvusClientService().query(wrapper);
    }

    public Page<T> queryPage(LambdaQueryWrapper<T> wrapper, Long page, Long pageSize) throws MilvusException {
        wrapper.setEntityClass(entityType);
        return getMilvusClientService().queryPage(wrapper, page, pageSize);
    }

    protected MilvusClientService getMilvusClientService() {
        if (this.milvusClientService != null) {
            return this.milvusClientService;
        }
        this.milvusClientService = MilvusSelector.applicationContext.getBean(MilvusClientService.class);
        return this.milvusClientService;
    }
}