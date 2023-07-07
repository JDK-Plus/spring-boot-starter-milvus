package plus.jdk.milvus.record;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.DropCollectionParam;
import io.milvus.param.collection.HasCollectionParam;
import plus.jdk.cli.common.StringUtils;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.global.MilvusClientService;
import plus.jdk.milvus.model.CollectionDefinition;
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


    public void releaseCollection(Class<T> clazz) throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        milvusClientService.releaseCollection(clazz);
    }

    public boolean createIndex(Class<T> clazz, SFunction<T, ?> column,
                               IndexType indexType, MetricType metricType, String extraParam) throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.createIndex(clazz, column, indexType, metricType, extraParam);
    }

    public void dropCollection(Class<T> clazz) throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        milvusClientService.dropCollection(clazz);
    }

    public boolean hasCollection(Class<T> clazz) throws MilvusException {
        milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
        return milvusClientService.hasCollection(clazz);
    }

}
