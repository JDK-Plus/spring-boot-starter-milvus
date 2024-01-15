package plus.jdk.milvus.collection;

import io.milvus.grpc.DataType;
import io.milvus.param.MetricType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.jdk.milvus.annotation.VectorCollectionColumn;
import plus.jdk.milvus.annotation.VectorCollectionName;
import plus.jdk.milvus.record.VectorModel;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@VectorCollectionName(name = "user_blog2", description = "用户博文向量表")
public class UserBlogVector extends VectorModel<UserBlogVector> {

    /**
     * 主键
     */
    @VectorCollectionColumn(name = "id", dataType = DataType.Int64, primary = true)
    private Long id;

    /**
     * uid
     */
    @VectorCollectionColumn(name = "uid", dataType = DataType.Int64)
    private Long uid;

    /**
     * 博文文本
     */
    @VectorCollectionColumn(name = "blog_text", dataType = DataType.VarChar, maxLength = 1024)
    private String blogText;

    /**
     * 博文分类
     */
    @VectorCollectionColumn(name = "blog_type", dataType = DataType.Array, elementType = DataType.VarChar)
    private List<String> blogType;

    /**
     * 博文文本向量， 此处的博文文本向量使用m3e embedding, 所以是768
     */
    @VectorCollectionColumn(name = "v_blog_text", dataType = DataType.FloatVector, vectorDimension = 768, metricType = MetricType.COSINE)
    private List<Float> blogTextVector;
}
