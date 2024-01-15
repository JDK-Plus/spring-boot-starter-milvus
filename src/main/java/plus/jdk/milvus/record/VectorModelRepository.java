package plus.jdk.milvus.record;

import io.milvus.grpc.LoadState;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.model.IIndexExtra;
import plus.jdk.milvus.model.Page;
import plus.jdk.milvus.toolkit.support.SFunction;
import plus.jdk.milvus.wrapper.LambdaQueryWrapper;
import plus.jdk.milvus.wrapper.LambdaSearchWrapper;

import java.util.List;

public interface VectorModelRepository<T extends VectorModel<? extends VectorModel<?>>> {
    boolean insert(T vectorModel) throws MilvusException;

    boolean remove(Object pk) throws MilvusException;

    boolean batchRemove(LambdaQueryWrapper<T> wrapper) throws MilvusException;

    boolean createCollection() throws MilvusException;

    void loadCollection() throws MilvusException;

    LoadState getLoadState() throws MilvusException;

    Long getLoadProgress() throws MilvusException;


    void releaseCollection() throws MilvusException;

    boolean createIndex(String indexName, SFunction<T, ?> column, IIndexExtra extraParam);

    boolean dropIndex(String indexName) throws MilvusException;

    void dropCollection() throws MilvusException;

    boolean hasCollection() throws MilvusException;

    List<T> search(LambdaSearchWrapper<T> wrapper) throws MilvusException;

    List<T> query(LambdaQueryWrapper<T> wrapper) throws MilvusException;

    Page<T> queryPage(LambdaQueryWrapper<T> wrapper, Long page, Long pageSize) throws MilvusException;
}
