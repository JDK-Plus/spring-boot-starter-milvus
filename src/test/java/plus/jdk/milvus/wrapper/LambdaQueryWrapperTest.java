package plus.jdk.milvus.wrapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import plus.jdk.milvus.collection.UserBlogVector;

import java.util.Arrays;

@SpringBootTest
class LambdaQueryWrapperTest {

    @Test
    void test_json_wrapper() {
        LambdaQueryWrapper<UserBlogVector> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBlogVector::getUserId, 2656274875L)
                .or()
                .ne(UserBlogVector::getUserId, 1234567890L)
//下面json查询，运行时提示递归更新异常？
                .or(jsonWrapper ->
                        jsonWrapper
                                .jsonContains(UserBlogVector::getBlogType, 1, "type")
                                .jsonContainsAll(UserBlogVector::getBlogType, Arrays.asList("1", "2"), "type")
                                .or()
                                .jsonContainsAny(UserBlogVector::getBlogType, Arrays.asList("112", "312"), "tasd")
                );
        System.out.println(wrapper.getExprSegment());
    }
}