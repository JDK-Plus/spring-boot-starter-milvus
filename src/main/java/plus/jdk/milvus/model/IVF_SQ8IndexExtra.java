package plus.jdk.milvus.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * <a href="https://milvus.io/docs/index.md#floating">...</a>
 */
@Data
public class IVF_SQ8IndexExtra implements IIndexExtra {

    /**
     * Index building parameters
     * Number of cluster units	[1, 65536]
     */
    @SerializedName("nlist")
    private Integer nList;

    /**
     * Search parameters
     * Number of units to query	CPU: [1, nlist]
     */
    @SerializedName("nprobe")
    private Integer nProbe;
}
