package plus.jdk.milvus.enums;

import lombok.AllArgsConstructor;
import plus.jdk.milvus.conditions.IExprSegment;

/**
 * wrapper 内部使用枚举
 */
@AllArgsConstructor
public enum WrapperKeyword implements IExprSegment {
    /**
     * 只用作于辨识,不用于其他
     */
    APPLY(null);

    private final String keyword;

    @Override
    public String getExprSegment() {
        return keyword;
    }
}