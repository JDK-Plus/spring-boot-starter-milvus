package plus.jdk.milvus.model;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.AllArgsConstructor;
import lombok.Data;
import plus.jdk.milvus.common.Operator;

@Data
@AllArgsConstructor
public class WrapperModel<T> {

    /**
     * 根据哪一列来做查询
     */
    private SFunction<T, ?> column;

    /**
     * 操作符
     */
    private Operator operator;

    /**
     * 输入的值
     */
    private Object value;
}
