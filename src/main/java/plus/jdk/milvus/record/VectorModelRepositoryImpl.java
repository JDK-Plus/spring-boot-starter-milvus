package plus.jdk.milvus.record;

import io.milvus.grpc.MutationResult;
import io.milvus.param.R;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.global.MilvusClientService;
import plus.jdk.milvus.selector.MilvusSelector;

import java.io.Serializable;

public abstract class VectorModelRepositoryImpl<T extends VectorModel<? extends VectorModel>> implements Serializable {

    private final MilvusClientService milvusClientService;

    public VectorModelRepositoryImpl() {
        this.milvusClientService = MilvusSelector.beanFactory.getBean(MilvusClientService.class);
    }

    public R<MutationResult> insert(T vectorModel) throws MilvusException {
        return milvusClientService.insert(vectorModel);
    }

}
