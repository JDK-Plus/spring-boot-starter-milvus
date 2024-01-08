Milvus 2.0 is a cloud-native vector database, featuring a design architecture that separates storage from computation. All components of this revamped version are stateless, greatly enhancing system resilience and flexibility. For more details about the system architecture, refer to [Milvus System Architecture](https://milvus.io/cn/docs/architecture_overview.md).

Milvus is released under the [Apache 2.0 License](https://github.com/milvus-io/milvus/blob/master/LICENSE), it was officially open-sourced in October 2019 and now is a graduate project of [LF AI & Data Foundation](https://lfaidata.foundation/).


<h3 align="center">A Java-style Milvus Operation Library</h3>
<p align="center">
    <a href="https://github.com/JDK-Plus/spring-boot-starter-milvus/blob/master/LICENSE"><img src="https://img.shields.io/github/license/JDK-Plus/spring-boot-starter-milvus.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-milvus/releases"><img src="https://img.shields.io/github/release/JDK-Plus/spring-boot-starter-milvus.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-milvus/stargazers"><img src="https://img.shields.io/github/stars/JDK-Plus/spring-boot-starter-milvus.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-milvus/network/members"><img src="https://img.shields.io/github/forks/JDK-Plus/spring-boot-starter-milvus.svg" /></a>
</p>
This component is a Milvus component written in the style of mybatis-plus. It allows you to operate Milvus in java just like using mysql, executing precise query operations, or using vectors to execute similarity queries.

- [中文文档](README-CN.md)

### I. How to Import

```xml
<dependency>
    <groupId>plus.jdk</groupId>
    <artifactId>spring-boot-starter-milvus</artifactId>
    <version>1.1.0</version>
</dependency>
```

### II. Milvus Configuration

```bash
plus.jdk.milvus.enabled=true
plus.jdk.milvus.host=*
plus.jdk.milvus.port=19530
plus.jdk.milvus.user-name=root
plus.jdk.milvus.*=123456
```

### III. Define ORM Objects


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
@VectorCollectionName(name = "user_blog", description = "User blog vector table")
public class UserBlogVector extends VectorModel<UserBlogVector> {

    /**
     * Primary Key
     */
    @VectorCollectionColumn(name = "id", dataType = DataType.Int64, primary = true)
    private Long id;

    /**
     * uid
     */
    @VectorCollectionColumn(name = "uid", dataType = DataType.Int64)
    private Long uid;

    /**
     * Blog text
     */
    @VectorCollectionColumn(name = "blog_text", dataType = DataType.VarChar, maxLength = 1024)
    private String blogText;

    /**
     * Blog type
     */
    @VectorCollectionColumn(name = "blog_type", dataType = DataType.JSON)
    private JSONObject blogType;

    /**
     * Blog text vector, the blog text vector used here is m3e embedding, so it is 768
     */
    @VectorCollectionColumn(name = "v_blog_text", dataType = DataType.FloatVector, vectorDimension = 768)
    private List<Float> blogTextVector;
}
```

### IV. Define DAO Layer

We have encapsulated many basic operation APIs for `milvus` in `VectorModelRepositoryImpl`.

```java
import com.weibo.biz.omniscience.dolly.milvus.entity.UserBlogVector;
import plus.jdk.milvus.annotation.VectorRepository;
import plus.jdk.milvus.record.VectorModelRepositoryImpl;

@VectorRepository
public class UserBlogVectorDao extends VectorModelRepositoryImpl<UserBlogVector> {
}
```

**Some commonly used API examples are as follows:**

```java
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
     * Create collection and index
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
     * Insert record into collection
     */
    @Test
    public void insertVector() throws MilvusException {
        String text = "Hi guys!! Just out of the oven nine pictures. Vote! Like figure few";
        Long uid = 2656274875L;
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
     * Use other fields to lookup related content
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
     * Use a vector to find the most similar content. You can also combine it with other fields for query filtering
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
     * Use primary key to delete record
     */
    @Test
    public void deleteRecord() throws MilvusException {
        boolean ret = userBlogVectorDao.remove(12345556);
        log.info("{}", ret);
    }
}
```
