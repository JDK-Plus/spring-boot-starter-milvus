package plus.jdk.milvus.conditions.segments;

import lombok.AccessLevel;
import lombok.Getter;
import plus.jdk.milvus.conditions.IExprSegment;
import plus.jdk.milvus.toolKit.StringPool;

import java.util.Arrays;
import java.util.List;

/**
 * 合并 EBNF 片段
 */
@Getter
public class MergeSegments implements IExprSegment {

    private final NormalSegmentList normal = new NormalSegmentList();

    @Getter(AccessLevel.NONE)
    private String exprSegment = StringPool.EMPTY;
    @Getter(AccessLevel.NONE)
    private boolean cacheExprSegment = true;

    public void add(IExprSegment... iExprSegments) {
        List<IExprSegment> list = Arrays.asList(iExprSegments);
        normal.addAll(list);
        cacheExprSegment = false;
    }

    @Override
    public String getExprSegment() {
        if (cacheExprSegment) {
            return exprSegment;
        }
        cacheExprSegment = true;
        exprSegment = normal.getExprSegment();
        return exprSegment;
    }

    /**
     * 清理
     *
     * @since 3.3.1
     */
    public void clear() {
        exprSegment = StringPool.EMPTY;
        cacheExprSegment = true;
        normal.clear();
    }
}
