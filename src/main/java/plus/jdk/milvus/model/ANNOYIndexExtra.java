package plus.jdk.milvus.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * <a href="https://milvus.io/docs/index.md#floating">...</a>
 */
@Data
public class ANNOYIndexExtra implements IIndexExtra {

    /**
     * Index building parameters
     * The number of trees.	[1, 1024]
     */
    @SerializedName("n_trees")
    private Integer nTrees;

    /**
     * Search parameters
     * The parameters that controls the search scope.	[k, inf]
     */
    @SerializedName("search_k")
    private Integer searchK;
}
