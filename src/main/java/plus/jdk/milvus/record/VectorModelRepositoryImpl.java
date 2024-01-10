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

public abstract class VectorModelRepositoryImpl<T extends VectorModel<? extends VectorModel<?>>> implements Serializable {

    private final Class<T> entityType;
    private MilvusClientService milvusClientService;

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
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.insert(vectorModel);
    }

    public boolean remove(Object pk) throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.remove(pk, entityType);
    }

    public boolean batchRemove(LambdaQueryWrapper<T> wrapper) throws MilvusException {
        wrapper.setEntityClass(entityType);
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.batchRemove(wrapper);
    }

    public boolean createCollection() throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.createCollection(entityType);
    }

    public void loadCollection() throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        milvusClientService.loadCollection(entityType);
    }

    public LoadState getLoadState() throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.getLoadState(entityType);
    }

    public Long getLoadProgress() throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.getLoadProgress(entityType);
    }


    public void releaseCollection() throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        milvusClientService.releaseCollection(entityType);
    }

    public boolean createIndex(String indexName, SFunction<T, ?> column, IIndexExtra extraParam) throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.createIndex(entityType, indexName, column, extraParam);
    }

    public boolean dropIndex(String indexName) throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.dropIndex(entityType, indexName);
    }

    public void dropCollection() throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        milvusClientService.dropCollection(entityType);
    }

    public boolean hasCollection() throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.hasCollection(entityType);
    }

    public List<T> search(LambdaSearchWrapper<T> wrapper) throws MilvusException {
        wrapper.setEntityClass(entityType);
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.search(wrapper);
    }

    public List<T> query(LambdaQueryWrapper<T> wrapper) throws MilvusException {
        wrapper.setEntityClass(entityType);
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.query(wrapper);
    }

    public Page<T> queryPage(LambdaQueryWrapper<T> wrapper, Long page, Long pageSize) throws MilvusException {
        wrapper.setEntityClass(entityType);
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.queryPage(wrapper, page, pageSize);
    }
}