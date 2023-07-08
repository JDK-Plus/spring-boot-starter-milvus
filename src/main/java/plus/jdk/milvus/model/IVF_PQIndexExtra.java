package plus.jdk.milvus.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * <a href="https://milvus.io/docs/index.md#floating">...</a>
 */
@Data
public class IVF_PQIndexExtra implements IIndexExtra {

    /**
     * Index building parameters
     * Number of cluster units	[1, 65536]
     */
    @SerializedName("nlist")
    private Integer nList;

    /**
     * Index building parameters
     * Number of factors of product quantization, range in dim mod m == 0
     */
    @SerializedName("m")
    private Integer m;

    /**
     * Index building parameters
     * [Optional] Number of bits in which each low-dimensional vector is stored. range in [1, 16] (8 by default)
     */
    @SerializedName("nbits")
    private Integer nBits;

    /**
     * Search parameters
     * Number of units to query	, range in [1, nlist]
     */
    @SerializedName("nprobe")
    private Integer nProbe;
}
