package plus.jdk.milvus.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * <a href="https://milvus.io/docs/index.md#floating">...</a>
 * HNSW (Hierarchical Navigable Small World Graph) is a graph-based indexing algorithm. It builds a multi-layer navigation structure for an image according to certain rules. In this structure, the upper layers are more sparse and the distances between nodes are farther;
 * the lower layers are denser and the distances between nodes are closer. The search starts from the uppermost layer, finds the node closest to the target in this layer, and then enters the next layer to begin another search. After multiple iterations, it can quickly approach the target position.
 * In order to improve performance, HNSW limits the maximum degree of nodes on each layer of the graph to M. In addition, you can use efConstruction (when building index) or ef (when searching targets) to specify a search range.
 */
@Data
public class HNSWIIndexExtra implements IIndexExtra {

    /**
     * Index building parameters
     * Maximum degree of the node, range in [4, 64]
     */
    @SerializedName("M")
    private Integer m;

    /**
     * Index building parameters
     * Search scope, range in [8, 512]
     */
    @SerializedName("efConstruction")
    private Integer efConstruction;

    /**
     * Search parameters
     * Search scope, range in [top_k, 32768]
     */
    @SerializedName("ef")
    private Integer ef;
}
