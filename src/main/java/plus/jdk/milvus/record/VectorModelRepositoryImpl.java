package plus.jdk.milvus.record;

import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.global.MilvusClientService;
import plus.jdk.milvus.selector.MilvusSelector;

import java.io.Serializable;

public abstract class VectorModelRepositoryImpl<T extends VectorModel<? extends VectorModel<?>>> implements Serializable {

    private MilvusClientService milvusClientService;

    public boolean insert(T vectorModel) throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.insert(vectorModel);
    }

    public boolean createCollection(Class<T> clazz) throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.createCollection(clazz);
    }

    public void loadCollection(Class<T> clazz) throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        milvusClientService.loadCollection(clazz);
    }
}
