package plus.jdk.milvus.model;

import com.google.gson.annotations.SerializedName;
import io.milvus.param.MetricType;
import lombok.Data;

/**
 * <a href="https://milvus.io/docs/index.md#floating">...</a>
 */
@Data
public class FLATIndexExtra implements IIndexExtra {

    @SerializedName("metric_type")
    private MetricType metricType;
}
