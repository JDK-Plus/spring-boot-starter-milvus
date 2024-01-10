package plus.jdk.milvus.toolKit.expr;

import plus.jdk.milvus.enums.ExprLike;
import plus.jdk.milvus.toolKit.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ExprUtils工具类
 */
public abstract class ExprUtils implements Constants {

    private static final Pattern pattern = Pattern.compile("\\{@((\\w+?)|(\\w+?:\\w+?)|(\\w+?:\\w+?:\\w+?))}");

    /**
     * 用%连接like
     *
     * @param str  原字符串
     * @param type like 类型
     * @return like 的值
     */
    public static String concatLike(Object str, ExprLike type) {
        switch (type) {
            case LEFT:
                return PERCENT + str;
            case RIGHT:
                return str + PERCENT;
            default:
                return PERCENT + str + PERCENT;
        }
    }

    public static List<String> findPlaceholder(String expr) {
        Matcher matcher = pattern.matcher(expr);
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }
}
