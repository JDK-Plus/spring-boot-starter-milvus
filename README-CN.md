Milvus 2.0 是一款云原生向量数据库，采用存储与计算分离的架构设计。该重构版本的所有组件均为无状态组件，极大地增强了系统弹性和灵活性。更多系统架构细节，参考 [Milvus 系统架构](https://milvus.io/cn/docs/architecture_overview.md)。

Milvus 基于 [Apache 2.0 License](https://github.com/milvus-io/milvus/blob/master/LICENSE) 协议发布，于 2019 年 10 月正式开源，是 [LF AI & Data 基金会](https://lfaidata.foundation/) 的毕业项目。


<h3 align="center">一个java风格的Milvus操作库</h3>
<p align="center">
    <a href="https://github.com/JDK-Plus/spring-boot-starter-milvus/blob/master/LICENSE"><img src="https://img.shields.io/github/license/JDK-Plus/spring-boot-starter-milvus.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-milvus/releases"><img src="https://img.shields.io/github/release/JDK-Plus/spring-boot-starter-milvus.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-milvus/stargazers"><img src="https://img.shields.io/github/stars/JDK-Plus/spring-boot-starter-milvus.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-milvus/network/members"><img src="https://img.shields.io/github/forks/JDK-Plus/spring-boot-starter-milvus.svg" /></a>
</p>
该组件是一个仿照mybatis-plus风格编写的Milvus组件， 可以让你像使用mysql那样使用java来操作Milvus，执行精确的query查询或者使用向量执行相似性查询。

- [English](README.md)


### 一、如何引入

```xml
<dependency>
    <groupId>plus.jdk</groupId>
    <artifactId>spring-boot-starter-milvus</artifactId>
    <version>${last.version}</version>
</dependency>
```

### 二、milvus的引用配置

```bash
plus.jdk.milvus.enabled=true
plus.jdk.milvus.host=192.168.1.101
plus.jdk.milvus.port=19530
plus.jdk.milvus.user-name=root
plus.jdk.milvus.password=123456
```

### 三、定义ORM对象


```java
import io.milvus.grpc.DataType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import plus.jdk.milvus.annotation.VectorCollectionColumn;
import plus.jdk.milvus.annotation.VectorCollectionName;
import plus.jdk.milvus.record.VectorModel;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@VectorCollectionName(name = "user_blog", description = "用户博文向量表")
public class UserBlogVector extends VectorModel<UserBlogVector> {

    /**
     * 主键
     */
    @VectorCollectionColumn(name = "id", dataType = DataType.Int64, primary = true)
    private Long id;

    /**
     * uid
     */
    @VectorCollectionColumn(name = "uid", dataType = DataType.Int64)
    private Long uid;

    /**
     * 博文文本
     */
    @VectorCollectionColumn(name = "blog_text", dataType = DataType.VarChar, maxLength = 1024)
    private String blogText;

    /**
     * 博文类型
     */
    @VectorCollectionColumn(name = "blog_type", dataType = DataType.JSON)
    private JSONObject blogType;

    /**
     * 博文文本向量， 此处的博文文本向量使用m3e embedding, 所以是768
     */
    @VectorCollectionColumn(name = "v_blog_text", dataType = DataType.FloatVector, vectorDimension = 768)
    private List<Float> blogTextVector;
}
```

### 四、定义申明Dao数据层

我们在 `VectorModelRepositoryImpl` 封装了很多对 `milvus`进行基本操作的api

```java
import com.weibo.biz.omniscience.dolly.milvus.entity.UserBlogVector;
import plus.jdk.milvus.annotation.VectorRepository;
import plus.jdk.milvus.record.VectorModelRepositoryImpl;

@VectorRepository
public class UserBlogVectorDao extends VectorModelRepositoryImpl<UserBlogVector> {
}
```

**一些常用的api示例如下：**

```java
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import plus.jdk.milvus.collection.UserBlogVector;
import plus.jdk.milvus.common.MilvusException;
import plus.jdk.milvus.common.chat.ChatClient;
import plus.jdk.milvus.dao.UserBlogVectorDao;
import plus.jdk.milvus.model.HNSWIIndexExtra;

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
        List<UserBlogVector> queryResults = userBlogVectorDao.query(wrapper);
        log.info("{}", queryResults);
    }

    /**
     * 使用向量查找相似度最高的内容。可以结合其他字段做条件查询过滤
     */
    @Test
    public void search() throws MilvusException {
        String text = "Hi guys!! Just out of the oven nine pictures. Vote! Like figure few";
        LambdaSearchWrapper<UserBlogVector> wrapper = new LambdaSearchWrapper<>();
        List<List<Float>> embedding = chatClient.getEmbedding(Collections.singletonList(text));
        wrapper.vector(UserBlogVector::getBlogTextVector, embedding.get(0));
        wrapper.setTopK(10);
        wrapper.eq(UserBlogVector::getUid, 2656274875L);
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
```
