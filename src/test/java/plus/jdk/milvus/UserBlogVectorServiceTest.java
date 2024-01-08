package plus.jdk.milvus;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import plus.jdk.milvus.collection.UserBlogVector;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.common.chat.ChatClient;
import plus.jdk.milvus.wrapper.LambdaQueryWrapper;
import plus.jdk.milvus.dao.UserBlogVectorDao;
import plus.jdk.milvus.model.HNSWIIndexExtra;
import plus.jdk.milvus.wrapper.LambdaSearchWrapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Slf4j
@SpringBootTest
public class UserBlogVectorServiceTest {

    @Autowired
    private UserBlogVectorDao userBlogVectorDao;

    @Autowired
    private ChatClient chatClient;


    /**
     * 创建集合和索引
     */
    @Test
    public void createCollection() throws MilvusException {
        boolean ret = userBlogVectorDao.createCollection();
        HNSWIIndexExtra extra = new HNSWIIndexExtra();
        extra.setM(16);
        extra.setEfConstruction(8);
        userBlogVectorDao.createIndex("idx_blog_vector",
                UserBlogVector::getBlogTextVector, extra);
        userBlogVectorDao.loadCollection();
    }


    /**
     * 向集合插入记录
     */
    @Test
    public void insertVector() throws MilvusException {
        String text = "宝贝们！！没睡吧啊啊啊 刚出炉的九图 投票！喜欢图几";
        Long uid = 2656274875L;
//        long timestamp = System.currentTimeMillis();
//        Date startTime = new Date(timestamp - 3600 * 24 * 10 * 1000L); //最近3天的发博数据
//        Date endTime = new Date(timestamp);
        UserBlogVector userBlogVector = new UserBlogVector();
        userBlogVector.setBlogText(text);
        userBlogVector.setUid(uid);
        userBlogVector.setBlogType(new JSONObject() {{
            put("type", Arrays.asList("1", "2"));
        }});
        List<List<Float>> embedding = chatClient.getEmbedding(Collections.singletonList(text));
        userBlogVector.setBlogTextVector(embedding.get(0));
        boolean ret = userBlogVectorDao.insert(userBlogVector);
        log.info("{}", ret);
    }

    /**
     * 使用其他字段查找相关内容
     */
    @Test
    public void query() throws MilvusException {
        LambdaQueryWrapper<UserBlogVector> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserBlogVector::getUid, 2656274875L)
                .or()
                .ne(UserBlogVector::getUid, 1234567890L)
                .or(jsonWrapper ->
                        jsonWrapper
                                .jsonContains(UserBlogVector::getBlogType, 1, "type")
                                .jsonContainsAll(UserBlogVector::getBlogType, Arrays.asList("1", "2"), "type")
                                .or()
                                .jsonContainsAny(UserBlogVector::getBlogType, Arrays.asList("112", "312"), "tasd")
                );
        Assertions.assertEquals(wrapper.getExprSegment(), "(uid = 2656274875 OR uid != 1234567890 OR (JSON_CONTAINS (blogType['type'], 1) AND JSON_CONTAINS_ALL (blogType['type'], ['1','2']) OR JSON_CONTAINS_ANY (blogType['tasd'], ['112','312'])))", "");
//        List<UserBlogVector> queryResults = userBlogVectorDao.query(wrapper);
        log.info("{}", wrapper.getExprSegment());
    }

    /**
     * 使用向量查找相似度最高的内容。可以结合其他字段做条件查询过滤
     */
    @Test
    public void search() throws MilvusException {
        String text = "宝贝们！！没睡吧啊啊啊 刚出炉的九图 投票！喜欢图几";
        LambdaSearchWrapper<UserBlogVector> wrapper = new LambdaSearchWrapper<>();
        List<List<Float>> embedding = chatClient.getEmbedding(Collections.singletonList(text));
        wrapper.vector(UserBlogVector::getBlogTextVector, embedding.get(0))
                .setTopK(10)
                .eq(UserBlogVector::getUid, 2656274875L);
        wrapper.jsonContainsAny(UserBlogVector::getBlogType, Arrays.asList("1", "2"), "type");
        List<UserBlogVector> searchResults = userBlogVectorDao.search(wrapper);
        log.info("{}", searchResults);
    }


    /**
     * 使用主键删除记录
     * =
     */
    @Test
    public void deleteRecord() throws MilvusException {
        boolean ret = userBlogVectorDao.remove(12345556);
        log.info("{}", ret);
    }

}