package plus.jdk.milvus.toolkit.support;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ColumnCache implements Serializable {

    private static final long serialVersionUID = -4586291538088403456L;

    /**
     * 使用 column
     */
    private String column;

}