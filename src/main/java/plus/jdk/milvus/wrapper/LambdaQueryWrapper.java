package plus.jdk.milvus.wrapper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.jdk.milvus.record.VectorModel;


@Data
@EqualsAndHashCode(callSuper = true)
public class LambdaQueryWrapper<T extends VectorModel<?>> extends AbstractLambdaWrapper<T> {

    private Long offset;

    private Long limit;
}
