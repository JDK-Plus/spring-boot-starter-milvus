package plus.jdk.milvus.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Page<T> {

    /**
     * 第几页
     */
    private Long page = 0L;

    /**
     * 每页多少条数据
     */
    private Long pageSize = 20L;

    /**
     * 数据列表
     */
    private List<T> instances = new ArrayList<>();


    public boolean hasNext() {
        return !instances.isEmpty();
    }
}
